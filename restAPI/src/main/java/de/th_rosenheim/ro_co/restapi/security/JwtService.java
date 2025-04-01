/**
 * This class is responsible for generating and validating JWT tokens.
 * It uses the io.jsonwebtoken library to create and parse JWTs.
 * The secret key and expiration time are injected from application properties.
 * @See {@link https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac}
 */

package de.th_rosenheim.ro_co.restapi.security;

import io.jsonwebtoken.Jws;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private static final SignatureAlgorithm SIG_ALG = Jwts.SIG.ES512; //or ES256 or ES384

    @Value("${security.jwt.secret-key}")
    private String secretKey; // Base64 encoded private key

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;


    /**
     * Helpers to extract claims of the JWT token.
     * @param token
     * @param claimsResolver
     * @return
     * @param <T>
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the claimed username from the JWT token.
     * @param token
     * @return
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    /**
     * Generates a JWT token for the given user details. Uses {@link #generateToken(Map, UserDetails)} to do so.
     * @param userDetails
     * @return
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token with the given extra claims and user details. Uses {@link #buildToken(Map, UserDetails, long)} to do so.
     * @param extraClaims
     * @param userDetails
     * @return
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * @return the expiration time of the springboot application.properties.
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Generic function to build the JWT token with the given extra claims and user details.
     * @param extraClaims
     * @param userDetails
     * @param expiration
     * @return
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
            return Jwts.
                    builder()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSignInKey(), SIG_ALG)
                    .compact();

    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        SecretKey key = (SecretKey) getSignInKey();
        Jws<Claims> claims = Jwts.parser().
                verifyWith(key). // decryptWith(key).
                build().
                parseSignedClaims(token);
        return claims.getPayload();
    }

    private PrivateKey getSignInKey() {
        try {
            byte[] encoded = Decoders.BASE64.decode(secretKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //@TODO impl Logging
            throw new NotImplementedException("Not implemented yet", e);
        }
    }
}