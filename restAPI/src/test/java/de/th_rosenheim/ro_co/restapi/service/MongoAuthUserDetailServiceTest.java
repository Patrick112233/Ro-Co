package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class MongoAuthUserDetailServiceTest {

    private UserRepository userRepository;
    private Validator validator;
    private MongoAuthUserDetailService service;

    @BeforeEach
    void setUp() throws Exception {
        userRepository = mock(UserRepository.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new MongoAuthUserDetailService(userRepository, validator);
    }

    @Test
    void loadUserByUsername_allCases() {
        // Normalfall mit gültigen Werten
        User validUser = new User("test@example.com", "Pw123456!", "TestUser","USER");
        validUser.setVerified(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(validUser));
        assertEquals(validUser, service.loadUserByUsername("test@example.com"));

        // User mit ungültigen Werten (z.B. leeres Passwort)
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(""));
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(null));


        // Nicht vorhandener User
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("notfound@example.com"));
    }
}