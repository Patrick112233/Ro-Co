/**
 * This class is responsible for generating and validating JWT tokens.
 * It uses the io.jsonwebtoken library to create and parse JWTs.
 * The secret key and expiration time are injected from application properties.
 * @See {@link https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac}
 */

package de.th_rosenheim.ro_co.restapi.security;

import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureAlgorithm;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class JwtService {

    public static final String BEARER_STRING = "Bearer ";


    Logger logger = LoggerFactory.getLogger(JwtService.class);

    private static final SignatureAlgorithm SIG_ALG = Jwts.SIG.ES512; //or ES256 or ES384

    @Value("${security.jwt.private-key}")
    private String secretKey;

    @Value("${security.jwt.public-key}")
    private String publicKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    private final UserRepository userRepository;

    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


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
     * Extracts the token from the bearer string.
     * @param token
     * @return
     */
    public String extractToken(String bearer) {
        return bearer.substring(BEARER_STRING.length());
    }

    /**
     * Checks if the token contains the bearer string.
     * @param bearer
     * @return
     */
    public boolean isBearer(String bearer) {
        return bearer.startsWith(BEARER_STRING);
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
        Optional<User> user = userRepository.findByEmail(username);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && user.isPresent() && user.get().isVerified();
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
        PrivateKey key = getSignInKey();
        return Jwts.
                    builder()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(key, SIG_ALG)
                    .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        PublicKey key = getVerificationKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PrivateKey getSignInKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            logger.error(e.getMessage());
            return null;
        }
    }


    private PublicKey getVerificationKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}