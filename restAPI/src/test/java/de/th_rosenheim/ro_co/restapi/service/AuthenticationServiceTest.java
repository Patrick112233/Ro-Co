package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.RegisterUserDto;
import de.th_rosenheim.ro_co.restapi.model.RefreshToken;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.RefreshTokenRepository;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static de.th_rosenheim.ro_co.restapi.model.User.instantiateUser;
import static de.th_rosenheim.ro_co.restapi.service.JwtService.hashToken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private AuthenticationService authenticationService;
    private UserService userService;
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        jwtService = mock(JwtService.class);

        authenticationService = new AuthenticationService(userRepository, authenticationManager, refreshTokenRepository, jwtService);
        java.lang.reflect.Field userField;
        java.lang.reflect.Field authField;
        java.lang.reflect.Field refreshTokenField;
        java.lang.reflect.Field jwtServiceField;
        try {
            userField = AuthenticationService.class.getDeclaredField("userRepository");
            userField.setAccessible(true);
            userField.set(authenticationService, userRepository);

            authField = AuthenticationService.class.getDeclaredField("authenticationManager");
            authField.setAccessible(true);
            authField.set(authenticationService, authenticationManager);

            refreshTokenField = AuthenticationService.class.getDeclaredField("refreshTokenRepository");
            refreshTokenField.setAccessible(true);
            refreshTokenField.set(authenticationService, refreshTokenRepository);

            jwtServiceField = AuthenticationService.class.getDeclaredField("jwtService");
            jwtServiceField.setAccessible(true);
            jwtServiceField.set(authenticationService, jwtService);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void signup() {
        // 1. Normalfall
        var dto = new RegisterUserDto("test@example.com", "Pw123456!!", "TestUser");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        var user = instantiateUser("test@example.com", "Pw123456!!", "myName","USER");
        user.setId("507f1f77bcf86cd799439011");
        when(userRepository.insert((de.th_rosenheim.ro_co.restapi.model.User) any())).thenReturn(user);

        var result = authenticationService.signup(dto);
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("TestUser", result.get().getUsername());

        // 2. Doppelte E-Mail
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        assertThrows(de.th_rosenheim.ro_co.restapi.exceptions.NonUniqueException.class, () -> authenticationService.signup(dto));

        // 3. Ungültige DTOs
        var invalidEmail = new RegisterUserDto("invalid", "pw", "ab");
        assertThrows(Exception.class, () -> authenticationService.signup(invalidEmail));
        var nulls = new RegisterUserDto(null, null, null);
        assertThrows(Exception.class, () -> authenticationService.signup(nulls));
        var shortName = new RegisterUserDto("test@example.com", "pw", "a");
        assertThrows(Exception.class, () -> authenticationService.signup(shortName));

        // 4. Repository Fehler bei Insert
        var dto2 = new RegisterUserDto("test2@example.com", "Pw123456!!", "TestUser2");
        when(userRepository.findByEmail("test2@example.com")).thenReturn(Optional.empty());
        when(userRepository.insert((User) any())).thenThrow(new RuntimeException("DB down"));
        assertThrows(RuntimeException.class, () -> authenticationService.signup(dto2));

    }

    @Test
    void authenticate() {
        // Gemeinsame Testdaten
        var validEmail = "test@example.com";
        var validPassword = "Pw123456!!";
        var validUser = instantiateUser(validEmail, validPassword, "TestUser","USER");
        validUser.setVerified(true);
        validUser.setId("507f1f77bcf86cd799439011");
        var loginDto = new LoginUserDto(validEmail, validPassword);

        // 1. Normalfall
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(validUser));
        when(jwtService.generateToken(validUser)).thenReturn("jwtToken");
        try {
            when(jwtService.generateRefreshToken(validUser)).thenReturn("refreshToken");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // AuthenticationManager wirft keine Exception
        var result = authenticationService.authenticate(loginDto);
        assertTrue(result.isPresent());
        assertEquals(validEmail, result.get().getEmail());
        assertEquals("TestUser", result.get().getUsername());
        assertEquals("USER", result.get().getRole());
        assertEquals("jwtToken", result.get().getToken());
        assertEquals("refreshToken", result.get().getRefreshToken());

        // 2. Invalid input
        var invalidLogin = new LoginUserDto("wrong.exampl.mail", "wrong");
        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> authenticationService.authenticate(invalidLogin));

        // 3. User nicht gefunden
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(loginDto));

        // 4. User nicht verifiziert
        var notVerifiedUser = instantiateUser(validEmail, validPassword, "TestUser2","USER");
        notVerifiedUser.setVerified(false);
        var loginDtoNotVerified = new LoginUserDto(validEmail, validPassword);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(notVerifiedUser));
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(loginDtoNotVerified));

        // 5. Invalides User-Objekt aus Datenbank (z.B. null-Felder)
        var invalidUser = instantiateUser("Not@mail.com", "Pw123456!", "myName","USER");
        //set via reflection userer id = null
        Field field = null;
        try {
            field = User.class.getDeclaredField("email");
            field.setAccessible(true);
            field.set(invalidUser, "not.valid.email");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        invalidUser.setVerified(true);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(invalidUser));
        assertThrows(jakarta.validation.ValidationException.class, () -> authenticationService.authenticate(loginDto));

        // 3. Falsches Passwort
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(validUser));
        doThrow(new AuthenticationException("Bad credentials") {
        }).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(loginDto));
    }


    @Test
    void refresh() throws Exception {
        String validEmail = "test@example.com";
        String refreshToken = "refreshToken";
        String bearerHeader = "Bearer " + refreshToken;
        User user = instantiateUser(validEmail, "Pw123456!!", "TestUser","USER");
        user.setId("507f1f77bcf86cd799439011");
        user.setVerified(true);
        user.setRole("USER");
        user.setPassword("Pw123456!!");

        // 1. DB hat keinen kentniss von den refresh token!
        when(jwtService.isBearer(bearerHeader)).thenReturn(true);
        when(jwtService.extractToken(bearerHeader)).thenReturn(refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(validEmail);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(true);
        assertThrows(ExpiredJwtException.class, () -> authenticationService.refresh(bearerHeader));


        // 1. Normalfall (Valid refresh toke)
        var dummytoken = new RefreshToken();
        dummytoken.setId("1");
        dummytoken.setTokenHash(hashToken(refreshToken));
        user.addRefreshToken(dummytoken);
        when(jwtService.isBearer(bearerHeader)).thenReturn(true);
        when(jwtService.extractToken(bearerHeader)).thenReturn(refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(validEmail);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("newJwtToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("newRefreshToken");
        Optional<?> result = authenticationService.refresh(bearerHeader);
        assertTrue(result.isPresent());

        // 3. Ungültiges Auth-Header (Nicht-Bearer)
        when(jwtService.isBearer("invalidHeader")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> authenticationService.refresh("invalidHeader"));

        // 4. User nicht gefunden
        when(jwtService.isBearer(bearerHeader)).thenReturn(true);
        when(jwtService.extractToken(bearerHeader)).thenReturn(refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn("notfound@example.com");
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.refresh(bearerHeader));

        // 5. Refresh-Token ungültig
        when(jwtService.extractUsername(refreshToken)).thenReturn(validEmail);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> authenticationService.refresh(bearerHeader));

        // 6. Invalides User-Objekt (z.B. null-Felder)
        var invalidUser = instantiateUser("Not@mail.com", "Pw123456!", "myName","USER");
        Field field = null;
        try {
            field = User.class.getDeclaredField("email");
            field.setAccessible(true);
            field.set(invalidUser, "not.valid.email");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        invalidUser.setVerified(true);
        invalidUser.addRefreshToken(dummytoken);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(invalidUser));
        when(jwtService.isRefreshTokenValid(refreshToken, invalidUser)).thenReturn(true);
        assertThrows(ValidationException.class, () -> authenticationService.refresh(bearerHeader));
    }


    @Test
    void isDisplayNameAvailable() {
        // 1. Normalfall: count = 0 -> true
        when(userRepository.countByDisplayName("TestUser")).thenReturn(0L);
        assertTrue(authenticationService.isDisplayNameAvailable("TestUser"));

        // 2. Normalfall: count = 1 -> false
        when(userRepository.countByDisplayName("TestUser")).thenReturn(1L);
        assertFalse(authenticationService.isDisplayNameAvailable("TestUser"));

        // 3. Falsche Eingabe: null
        assertThrows(de.th_rosenheim.ro_co.restapi.exceptions.NonUniqueException.class, () -> authenticationService.isDisplayNameAvailable(null));

        // 3. Falsche Eingabe: leerer String
        assertThrows(de.th_rosenheim.ro_co.restapi.exceptions.NonUniqueException.class, () -> authenticationService.isDisplayNameAvailable(""));
    }
}