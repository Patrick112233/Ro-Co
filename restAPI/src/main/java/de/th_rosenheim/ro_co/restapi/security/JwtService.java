/**
 * This class is responsible for generating and validating JWT tokens.
 * It uses the io.jsonwebtoken library to create and parse JWTs.
 * The secret key and expiration time are injected from application properties.
 * @See {@link https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac}
 */

package de.th_rosenheim.ro_co.restapi.security;

import de.th_rosenheim.ro_co.restapi.model.RefreshToken;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.RefreshTokenRepository;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
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


    final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private static final SignatureAlgorithm SIG_ALG = Jwts.SIG.ES512; //or ES256 or ES384

    @Value("${security.jwt.private-key}")
    private String secretKey;

    @Value("${security.jwt.public-key}")
    private String publicKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value("${security.jwt.expiration-refresh-time}")
    private long jwtRefreshExpiration;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    /**
     * Helper method to extract claims from a JWT token.
     * @param token the JWT token
     * @param claimsResolver a function to resolve specific claims from the token
     * @param <T> the type of the claim to be resolved
     * @return the resolved claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the claimed username from the JWT token.
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the token from the bearer string.
     * @param bearer the bearer string containing the token
     * @return the extracted token
     */
    public String extractToken(String bearer) {
        return bearer.substring(BEARER_STRING.length());
    }

    /**
     * Checks if the given string contains the bearer prefix.
     * @param bearer the string to check
     * @return true if the string starts with the bearer prefix, false otherwise
     */
    public boolean isBearer(String bearer) {
        return bearer.startsWith(BEARER_STRING);
    }


    /**
     * Generates a JWT token for the given user details. Uses {@link #generateToken(Map, UserDetails, long)} to do so.
     * @param userDetails the user details for which the token is generated
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        String token = generateToken(Collections.singletonMap("isRefresh", true), userDetails, jwtRefreshExpiration);

        // Save the refresh token in the database for invalidation
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken_hash(hashToken(token));

        refreshTokenRepository.save(refreshToken);
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.addRefreshToken(refreshToken);
        userRepository.save(user);

        return token;
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generates a JWT token with the given extra claims and user details.
     * Uses {@link #buildToken(Map, UserDetails, long)} to do so.
     * @param extraClaims a map of additional claims to include in the token
     * @param userDetails the user details for which the token is generated
     * @param expiration the expiration time for the token in milliseconds
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return buildToken(extraClaims, userDetails, expiration);
    }

    /**
     * @return the expiration time of the springboot application.properties.
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    public boolean isLoginTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final Claims claims = extractAllClaims(token);
        boolean isRefresh = Optional.ofNullable(claims.get("isRefresh", Boolean.class)).orElse(false);
        Optional<User> user = userRepository.findByEmail(username);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && user.isPresent() && user.get().isVerified() && !isRefresh;
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final Claims claims = extractAllClaims(token);
        boolean isRefresh = claims.get("isRefresh", Boolean.class);
        Optional<User> user = userRepository.findByEmail(username);
        if ((username.equals(userDetails.getUsername())) && !isTokenExpired(token) && user.isPresent() && user.get().isVerified() && isRefresh){
            return true;
        }else{
            //delete refresh token from database
            String token_hash = hashToken(token);
            Optional<RefreshToken> refreshToken = Optional.ofNullable(refreshTokenRepository.findByToken(token_hash));
            refreshToken.ifPresent(refreshTokenRepository::delete);
            return false;
        }
        
    }

    /**
     * Generic function to build the JWT token with the given extra claims and user details.
     * @param extraClaims a map of additional claims to include in the token
     * @param userDetails the user details for which the token is generated
     * @param expiration the expiration time for the token in milliseconds
     * @return the generated JWT token
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