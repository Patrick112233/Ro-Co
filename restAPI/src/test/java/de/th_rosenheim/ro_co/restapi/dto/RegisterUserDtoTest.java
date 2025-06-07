package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUserDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void allRegisterUserDtoValidationCases() {
        // Gültiger Fall
        RegisterUserDto validDto = new RegisterUserDto("valid@mail.com", "StrongPass123!", "ValidUser");
        Set<ConstraintViolation<RegisterUserDto>> validViolations = validator.validate(validDto);
        assertTrue(validViolations.isEmpty(), "Es sollten keine Validierungsfehler auftreten.");

        // Ungültiger Username: zu kurz
        RegisterUserDto shortUsernameDto = new RegisterUserDto("valid@mail.com", "StrongPass123!", "Al");
        Set<ConstraintViolation<RegisterUserDto>> shortUsernameViolations = validator.validate(shortUsernameDto);
        assertFalse(shortUsernameViolations.isEmpty());
        assertTrue(shortUsernameViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));

        // Ungültiger Username: null
        RegisterUserDto nullUsernameDto = new RegisterUserDto("valid@mail.com", "StrongPass123!", null);
        Set<ConstraintViolation<RegisterUserDto>> nullUsernameViolations = validator.validate(nullUsernameDto);
        assertFalse(nullUsernameViolations.isEmpty());
        assertTrue(nullUsernameViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));

        // Ungültiger Username: zu lang
        String longName = "A".repeat(256);
        RegisterUserDto longUsernameDto = new RegisterUserDto("valid@mail.com", "StrongPass123!", longName);
        Set<ConstraintViolation<RegisterUserDto>> longUsernameViolations = validator.validate(longUsernameDto);
        assertFalse(longUsernameViolations.isEmpty());
        assertTrue(longUsernameViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));

        // Ungültiges Passwort
        RegisterUserDto invalidPasswordDto = new RegisterUserDto("valid@mail.com", "pw", "ValidUser");
        Set<ConstraintViolation<RegisterUserDto>> invalidPasswordViolations = validator.validate(invalidPasswordDto);
        assertFalse(invalidPasswordViolations.isEmpty());
        assertTrue(invalidPasswordViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));

        // Ungültige E-Mail
        RegisterUserDto invalidEmailDto = new RegisterUserDto("invalid-email", "StrongPass123!", "ValidUser");
        Set<ConstraintViolation<RegisterUserDto>> invalidEmailViolations = validator.validate(invalidEmailDto);
        assertFalse(invalidEmailViolations.isEmpty());
        assertTrue(invalidEmailViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
}