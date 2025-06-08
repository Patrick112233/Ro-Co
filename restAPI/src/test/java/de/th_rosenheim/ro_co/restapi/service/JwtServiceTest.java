package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.model.RefreshToken;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.RefreshTokenRepository;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static de.th_rosenheim.ro_co.restapi.model.User.instantiateUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    /*Mocking*/
    static final String X508_PUBLIC_KEY = "MIICsTCCAhKgAwIBAgIUR21wSocqd/qVh/X9qqgV8pMWua0wCgYIKoZIzj0EAwIwajELMAkGA1UEBhMCREUxEDAOBgNVBAgMB0JhdmFyaWExEjAQBgNVBAcMCVJvc2VuaGVpbTENMAsGA1UECgwEUm9DbzEUMBIGA1UECwwLSW5mb3JtYXRpY3MxEDAOBgNVBAMMB1Jlc3RBUEkwHhcNMjUwNjA1MTIyNDA2WhcNMjYwNjA1MTIyNDA2WjBqMQswCQYDVQQGEwJERTEQMA4GA1UECAwHQmF2YXJpYTESMBAGA1UEBwwJUm9zZW5oZWltMQ0wCwYDVQQKDARSb0NvMRQwEgYDVQQLDAtJbmZvcm1hdGljczEQMA4GA1UEAwwHUmVzdEFQSTCBmzAQBgcqhkjOPQIBBgUrgQQAIwOBhgAEAB3GJ10//qh6mKdKxXAxa7iKaYMQVJWI8/CRbRPt2QGg4xXILoefyyk3HUfvHf8IwN+qUJKEA52qQzVEvfvibr6MAV9vdrMOJdXiM8qOEqtuuVbTZ6TGCE4Me4k0xEbSh4Uyxrbl0IcwdEQ1dKZBntKsLgOL/4kmgsxnaz9xJ+lZItKko1MwUTAdBgNVHQ4EFgQUpNvBVbqscsBdPW3UEK5srHjVRvIwHwYDVR0jBBgwFoAUpNvBVbqscsBdPW3UEK5srHjVRvIwDwYDVR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgOBjAAwgYgCQgFYx6IM3dPoJKPOb+6e1utOvQPQWt6GsPuekFcMjVKaOxUk9j2o8FxX0vthoXXDeGQg0vpZQ5Hw3mMSGuqpCT7+igJCANdsk9FUGyBi0EXgdF5XmfJ9vUfRPnnJF+qmLelAtLi54+724M4pDwjqsaGrbRC2Jgy72AZL9f7/eTrXbYicxon3";
    static final String PKCS8_PRIVATE_KEY = "MIHuAgEAMBAGByqGSM49AgEGBSuBBAAjBIHWMIHTAgEBBEIB03FyloRwpH0/iQcOG099bgoHKSXYbHy8d7Yh8VFkoO1A/lHxtOd8ZZ+xRTH9tCSWeWElz9ZNyzxVZzWpW/uR/vKhgYkDgYYABAAdxiddP/6oepinSsVwMWu4immDEFSViPPwkW0T7dkBoOMVyC6Hn8spNx1H7x3/CMDfqlCShAOdqkM1RL374m6+jAFfb3azDiXV4jPKjhKrbrlW02ekxghODHuJNMRG0oeFMsa25dCHMHRENXSmQZ7SrC4Di/+JJoLMZ2s/cSfpWSLSpA==";
    static final String JWT_TOKEN_INVALID = "eyJhbGciOiJFUzUxMiJ9.eyJzdWIiOiJKb2huQERvZTQyLmNvbSIsImlhdCI6MTc0OTEzMDM3MiwiZXhwIjoxNzQ5MTMzOTcyfQ.AKYdq_TlHC-siSq6_wOy-j33qt8bGv9bXTMa1lfp2JuzqcCpmd9_ud6w4o1xUZHBpzgrmtDCG1janDs3Qs-EDwT_AFBBx7Ur6fBheNE0XmsrtyLa1Dm_iYA7hLRXPrmKr6s9utiMCKom1GkVmP-8c5DL_Jkz71fqfiRjWGbLLyjUM9so";

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        jwtService = new JwtService(userRepository, refreshTokenRepository);

        //reflection of secretKey and publicKey
        java.lang.reflect.Field repoField;
        try {
            repoField = JwtService.class.getDeclaredField("secretKey");
            repoField.setAccessible(true);
            repoField.set(jwtService, PKCS8_PRIVATE_KEY);

            repoField = JwtService.class.getDeclaredField("publicKey");
            repoField.setAccessible(true);
            repoField.set(jwtService, X508_PUBLIC_KEY);

            repoField = JwtService.class.getDeclaredField("jwtExpiration");
            repoField.setAccessible(true);
            repoField.set(jwtService, 1000 * 60 * 60); // 1 hour
            repoField = JwtService.class.getDeclaredField("jwtRefreshExpiration");
            repoField.setAccessible(true);
            repoField.set(jwtService, 1000 * 60 * 60 * 24 * 7); // 1 week

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    void extractUsername(){
        try {
            String validToken = createJwtToken(
                    PKCS8_PRIVATE_KEY,
                    "John@Doe42.com",
                    System.currentTimeMillis() / 1000, // issued just now
                    System.currentTimeMillis() / 1000 + 60 * 60, // 1 hour expiration
                    Map.of() // oder Map.of("isRefresh", true) für Refresh-Token
            );
            assertEquals("John@Doe42.com", jwtService.extractUsername(validToken));


            String expiredToken = createJwtToken(
                    PKCS8_PRIVATE_KEY,
                    "John@Doe42.com",
                    System.currentTimeMillis() / 1000 - 1000, // issued 1000 seconds ago
                    System.currentTimeMillis() / 1000 - 100, // expired 100 seconds ago
                    Map.of()
            );
            assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));

            String nullSubjToken = createJwtToken(
                    PKCS8_PRIVATE_KEY,
                    null,
                    System.currentTimeMillis() / 1000 - 1000, // issued 1000 seconds ago
                    System.currentTimeMillis() / 1000 - 100, // expired 100 seconds ago
                    Map.of()
            );
            assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.extractUsername(nullSubjToken));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void extractToken() {
        String token = "abc.def.ghi";
        String bearer = JwtService.BEARER_STRING + token;
        assertEquals(token, jwtService.extractToken(bearer));
    }

    @Test
    void isBearer() {
        String token = "abc.def.ghi";
        String bearer = JwtService.BEARER_STRING + token;
        assertTrue(jwtService.isBearer(bearer));
        assertFalse(jwtService.isBearer(token));
    }


    @Test
    void generateToken() throws Exception {
        // Arrange
        String subject = "John@Doe42.com";
        Map<String, Object> extraClaims =  Map.of("TestClaim", 42);

        // UserDetails-Mock
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername(subject)
                        .password("irrelevant")
                        .authorities("USER")
                        .build();

        String serviceToken = jwtService.generateToken(extraClaims, userDetails, 3600);

        // Claims extrahieren und vergleichen
        var claimsService =
                Objects.requireNonNull(verifyJwtToken(serviceToken));


        assertEquals(subject, claimsService.getSubject());
        assertTrue(claimsService.getExpiration().getTime() > System.currentTimeMillis());
        assertEquals(42,claimsService.get("TestClaim"));
    }

    @Test
    void generateRefreshToken() throws Exception {
        // Arrange
        String subject = "John@Doe42.com";
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername(subject)
                        .password("irrelevant")
                        .authorities("USER")
                        .build();


        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.findByEmail(subject)).thenReturn(Optional.of(instantiateUser("John@Doe42.com", "Pw123456!", "myName","USER")));
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Claims extrahieren
        var claims = Objects.requireNonNull(verifyJwtToken(refreshToken));

        // Assert
        assertEquals(subject, claims.getSubject());
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis() + 1000 * 60 * 60 * 12); // > 12 h
        assertEquals(Boolean.TRUE, claims.get("isRefresh"));

        //check if refresh token is in database
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue((long) savedUser.getRefreshTokens().size() > 0, "Refresh token should be saved in user");

        //test with invalid user
        when(userRepository.findByEmail(subject)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> jwtService.generateRefreshToken(userDetails));
    }

    @Test
    void isRefreshTokenValid() throws Exception {
        String subject = "John@Doe42.com";
        User user = instantiateUser(subject, "Pw123456!", "John Doe","USER");
        user.setRole("USER");
        user.setPassword("Pw123456!");
        user.setRole("USER");

        // 1. Normalfall: Gültiger Refresh-Token, User existiert, verified, Token nicht abgelaufen
        when(userRepository.findByEmail(subject)).thenReturn(Optional.of(user));
        user.setVerified(true);

        long now = System.currentTimeMillis() / 1000;
        String validRefreshToken = createJwtToken(
                PKCS8_PRIVATE_KEY,
                subject,
                now,
                now + 3600,
                Map.of("isRefresh", true)
        );
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername(subject)
                        .password("Pw123456!")
                        .authorities("USER")
                        .build();

        assertTrue(jwtService.isRefreshTokenValid(validRefreshToken, userDetails));

        // 2. User fehlt: UserRepository gibt empty zurück -> false, Token wird aus DB gelöscht
        when(userRepository.findByEmail(subject)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> jwtService.isRefreshTokenValid(validRefreshToken, userDetails));


        // 3. Abgelaufener Refresh-Token: Token ist abgelaufen, wird aus DB gelöscht
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setTokenHash(JwtService.hashToken(validRefreshToken));
        when(userRepository.findByEmail(subject)).thenReturn(Optional.of(user));
        String expiredRefreshToken = createJwtToken(
                PKCS8_PRIVATE_KEY,
                subject,
                now - 7200,
                now - 3600,
                Map.of("isRefresh", true)
        );
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(mockRefreshToken));
        assertFalse(jwtService.isRefreshTokenValid(expiredRefreshToken, userDetails));
        verify(refreshTokenRepository, times(1)).delete(mockRefreshToken);
    }

    @Test
    void hashToken() throws Exception {
        // 1. Normalfall: Komplexer String, Vergleich mit eigenem SHA-256-Hash
        String input = "Test123!@#äöüß€";
        String expectedHash;
        {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            expectedHash = Base64.getEncoder().encodeToString(hash);
        }
        assertEquals(expectedHash, JwtService.hashToken(input));

        // 2. Leerer String: Erwartet Exception
        assertThrows(IllegalArgumentException.class, () -> JwtService.hashToken(""));

        // 3. Null-Input: Erwartet Exception
        assertThrows(IllegalArgumentException.class, () -> JwtService.hashToken(null));
    }

    @Test
    void testGenerateToken() throws Exception {
        // Arrange
        String subject = "John@Doe42.com";
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername(subject)
                        .password("Pw123456!")
                        .authorities("USER")
                        .build();

        // Act
        String token = jwtService.generateToken(userDetails);

        // Claims extrahieren
        var claims = Objects.requireNonNull(verifyJwtToken(token));

        // Assert
        assertEquals(subject, claims.getSubject());
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
        assertNull(claims.get("isRefresh"));
    }

    @Test
    void isLoginTokenValid() throws Exception {
        String subject = "John@Doe42.com";
        User user = instantiateUser(subject, "Pw123456!", "John Doe","USER");
        user.setPassword("Pw123456!");
        user.setVerified(true);

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername(subject)
                        .password("Pw123456!")
                        .authorities("USER")
                        .build();

        long now = System.currentTimeMillis() / 1000;

        // 1. Normalfall: Gültiger Token, User existiert, verified, kein Refresh
        when(userRepository.findByEmail(subject)).thenReturn(Optional.of(user));
        String validToken = createJwtToken(
                PKCS8_PRIVATE_KEY,
                subject,
                now,
                now + 3600,
                Map.of()
        );
        assertTrue(jwtService.isLoginTokenValid(validToken, userDetails));

        // 2. check with refresh token
        String refreshToken = createJwtToken(
                PKCS8_PRIVATE_KEY,
                subject,
                now,
                now + 3600,
                Map.of("isRefresh", true)
        );
        assertFalse(jwtService.isLoginTokenValid(refreshToken, userDetails));

        // 3. User nicht gefunden
        when(userRepository.findByEmail(subject)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> jwtService.isLoginTokenValid(validToken, userDetails));

        // 4. Abgelaufener Token
        when(userRepository.findByEmail(subject)).thenReturn(Optional.of(user));
        String expiredToken = createJwtToken(
                PKCS8_PRIVATE_KEY,
                subject,
                now - 7200,
                now - 3600,
                Map.of()
        );
        assertFalse(jwtService.isLoginTokenValid(expiredToken, userDetails));

        // 5. Ungültiger Token (falscher Key signiert)
        String invalidToken = JWT_TOKEN_INVALID;
        when(userRepository.findByEmail(subject)).thenReturn(Optional.of(user));
        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> jwtService.isLoginTokenValid(invalidToken, userDetails));
    }


    private static String createJwtToken(String pkcs8PrivateKeyBase64, String subject, long issuedAtEpochSec, long expiresAtEpochSec, Map<String, Object> extraClaims) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(pkcs8PrivateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey privateKey = kf.generatePrivate(keySpec);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(issuedAtEpochSec * 1000))
                .expiration(new Date(expiresAtEpochSec * 1000))
                .signWith(privateKey, Jwts.SIG.ES512)
                .compact();
    }

    private static Claims verifyJwtToken(String token) throws Exception {
            try {
                byte[] certBytes = Base64.getDecoder().decode(X508_PUBLIC_KEY);
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
                PublicKey certKey = certificate.getPublicKey();
                return Jwts.parser()
                        .verifyWith(certKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception e) {
                return null;
            }
    }


}