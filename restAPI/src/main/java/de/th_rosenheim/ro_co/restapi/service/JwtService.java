/**
 * This class is responsible for generating and validating JWT tokens.
 * It uses the io.jsonwebtoken library to create and parse JWTs.
 * The secret key and expiration time are injected from application properties.
 * @See {@link https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac}
 */

package de.th_rosenheim.ro_co.restapi.service;

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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class JwtService {

    public static final String BEARER_STRING = "Bearer ";
    public static final String REFRESH_TOKEN_HEADER = "isRefresh";


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
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
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

    public String generateRefreshToken(UserDetails userDetails) throws NoSuchAlgorithmException {
        String token = generateToken(Collections.singletonMap(REFRESH_TOKEN_HEADER, true), userDetails, jwtRefreshExpiration);
        Date expires = extractClaim(token, Claims::getExpiration);

        // Save the refresh token in the database for invalidation
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hashToken(token));
        refreshToken.setTokenExpires(expires);

        refreshTokenRepository.save(refreshToken);
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User E-Mail not found"));
        user.addRefreshToken(refreshToken);
        userRepository.save(user);

        return token;
    }

    public static String hashToken(String token) throws NoSuchAlgorithmException {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);

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
    public long getTokenExpirationTime() {
        return jwtExpiration;
    }

    public long getRefreshTokenExpirationTime() {
        return jwtRefreshExpiration;
    }

    public boolean isLoginTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final Claims claims = extractAllClaims(token);
            boolean isRefresh = Optional.ofNullable(claims.get(REFRESH_TOKEN_HEADER, Boolean.class)).orElse(false);
            User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && user.isVerified() && !isRefresh;
        }
        catch (io.jsonwebtoken.ExpiredJwtException e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        try{
            final String username = extractUsername(token);
            final Claims claims = extractAllClaims(token);
            boolean isRefresh = claims.get(REFRESH_TOKEN_HEADER, Boolean.class);
            User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if ((username.equals(userDetails.getUsername())) && !isTokenExpired(token) && user.isVerified() && isRefresh){
                return true;
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            //delete refresh token from database
            Optional<RefreshToken> refreshToken;
            try {
                refreshToken = refreshTokenRepository.findByTokenHash(hashToken(token));
            } catch (NoSuchAlgorithmException ex) {
                logger.error(ex.getMessage());
                return false;
            }
            refreshToken.ifPresent(refreshTokenRepository::delete);
            return false;
        }
        return false;
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
        Optional<PrivateKey> optionalKey = getSignInKey();
        if (optionalKey.isEmpty()) {
            throw new IllegalStateException("Signing key could not be retrieved");
        }
        PrivateKey key = optionalKey.get();
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
        Date expiration;
        try{
            expiration = extractExpiration(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        }
        return !(new Date(System.currentTimeMillis())).before(expiration);
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

    private Optional<PrivateKey> getSignInKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return Optional.ofNullable(keyFactory.generatePrivate(keySpec));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }


    private PublicKey getVerificationKey() {

        try {
            byte[] certBytes = Base64.getDecoder().decode(publicKey);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
            return certificate.getPublicKey();
        } catch (Exception e) {
            logger.error("Failed to parse public key: " + e.getMessage(), e);
            return null;
        }
    }
}