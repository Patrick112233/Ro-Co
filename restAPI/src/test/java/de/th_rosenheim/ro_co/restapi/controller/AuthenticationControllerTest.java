package de.th_rosenheim.ro_co.restapi.controller;

import de.th_rosenheim.ro_co.restapi.dto.LoginOutDto;
import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import de.th_rosenheim.ro_co.restapi.dto.RegisterUserDto;
import de.th_rosenheim.ro_co.restapi.exceptions.NonUniqueException;
import de.th_rosenheim.ro_co.restapi.service.AuthenticationService;
import de.th_rosenheim.ro_co.restapi.service.JwtService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Nested
    @TestConfiguration
    class TestSecurityConfig {
        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("not@mail.com")
                            .password("Test1234!")
                            .roles("USER")
                            .build()
            );
        }
    }


    @Test
    @WithMockUser(username = "BypassSSH", roles = {"USER"})
    void register() throws Exception {
        // 1. Erfolgreiche Registrierung
        OutUserDto outUserDto = new OutUserDto("1", "TestUser", "USER", "test@mail.com", true);

        Mockito.when(authenticationService.signup(any(RegisterUserDto.class)))
                .thenReturn(Optional.of(outUserDto));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@mail.com\",\"username\":\"TestUser\",\"password\":\"Test1234!\"}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/login"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("TestUser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.verified").value(true));

        // 2. E-Mail bereits vergeben
        Mockito.when(authenticationService.signup(any(RegisterUserDto.class)))
                .thenThrow(new NonUniqueException("Email already exists"));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"used@mail.com\",\"username\":\"TestUser\",\"password\":\"Test1234!\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // 3. Ungültige Eingabe (z.B. ungültige E-Mail)
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid-email\",\"username\":\"TestUser\",\"password\":\"Test1234!\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    @WithMockUser(username = "BypassSSH", roles = {"USER"})
    void checkUserName() throws Exception {
        // 1. Normalfall: Username verfügbar
        Mockito.when(authenticationService.isDisplayNameAvailable("TestUser")).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/signup/username")
                        .param("username", "TestUser"))
                .andExpect(status().isOk());

        // 2. Ungültige Eingabe: Leerer String
        mockMvc.perform(get("/api/v1/auth/signup/username")
                        .param("username", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // 3. Username nicht verfügbar
        Mockito.when(authenticationService.isDisplayNameAvailable("UsedUser")).thenReturn(false);

        mockMvc.perform(get("/api/v1/auth/signup/username")
                        .param("username", "UsedUser"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "not@mail.com", roles = {"USER"}, password ="Test1234!")
    void authenticate() throws Exception {
        // 1. Normalfall: Erfolgreiche Authentifizierung
        OutUserDto user = new OutUserDto("1", "TestUser", "USER", "not@mail.com", true);
        var loginOutDto = new LoginOutDto(user);
        loginOutDto.setToken("jwtToken");
        loginOutDto.setRefreshToken("refreshToken");
        loginOutDto.setRefreshExpiresIn(2000L);
        loginOutDto.setTokenExpiresIn(1000L);

        Mockito.when(authenticationService.authenticate(any(LoginUserDto.class)))
                .thenReturn(Optional.of(loginOutDto));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@mail.com\",\"password\":\"Test1234!\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwtToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("not@mail.com"))
                .andExpect(jsonPath("$.username").value("TestUser"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.verified").value(true));

        // 2. Wrong Password: AuthenticationException
        Mockito.when(authenticationService.authenticate(any(LoginUserDto.class)))
                .thenThrow(new BadCredentialsException("Wrong password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@mail.com\",\"password\":\"wrongPW1234!\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // 3. Nutzer nicht gefunden: UsernameNotFoundException
        Mockito.when(authenticationService.authenticate(any(LoginUserDto.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"notfound@mail.com\",\"password\":\"Test1234!\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    @WithMockUser(username = "not@mail.com", roles = {"USER"}, password = "Test1234!")
    void refresh() throws Exception {
        // 1. Normalfall: Erfolgreiches Refresh
        OutUserDto user = new OutUserDto("1", "TestUser", "USER", "not@mail.com", true);
        var loginOutDto = new LoginOutDto(user);
        loginOutDto.setToken("jwtToken");
        loginOutDto.setRefreshToken("refreshToken");
        loginOutDto.setRefreshExpiresIn(2000L);
        loginOutDto.setTokenExpiresIn(1000L);

        Mockito.when(authenticationService.refresh("Bearer validRefreshToken"))
                .thenReturn(Optional.of(loginOutDto));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer validRefreshToken")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwtToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("not@mail.com"))
                .andExpect(jsonPath("$.username").value("TestUser"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.verified").value(true));

        // 2. IllegalArgumentException
        Mockito.when(authenticationService.refresh("Bearer illegal"))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer illegal")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // 3. UsernameNotFoundException
        Mockito.when(authenticationService.refresh("Bearer notfound"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer notfound")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // 4. ExpiredJwtException
        io.jsonwebtoken.ExpiredJwtException expiredJwtException =
                new io.jsonwebtoken.ExpiredJwtException(null, null, "Token abgelaufen");
        Mockito.when(authenticationService.refresh("Bearer expired"))
                .thenThrow(expiredJwtException);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer expired")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void refresh_unauthorized_without_user() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer someToken")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }


}