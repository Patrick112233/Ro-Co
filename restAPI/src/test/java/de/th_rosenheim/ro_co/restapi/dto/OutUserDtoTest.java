package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OutUserDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void allOutUserDtoValidationCases() {
        // Gültiger Fall
        OutUserDto validDto = new OutUserDto("1", "Max", "USER", "max@mail.com", true);
        Set<ConstraintViolation<OutUserDto>> validViolations = validator.validate(validDto);
        assertTrue(validViolations.isEmpty(), "Es sollten keine Validierungsfehler auftreten.");

        // Ungültige E-Mail
        OutUserDto invalidEmailDto = new OutUserDto("2", "Anna", "USER", "invalid-email", false);
        Set<ConstraintViolation<OutUserDto>> invalidEmailViolations = validator.validate(invalidEmailDto);
        assertFalse(invalidEmailViolations.isEmpty());
        assertTrue(invalidEmailViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));

        // Null-ID
        OutUserDto nullIdDto = new OutUserDto(null, "Ben", "USER", "ben@mail.com", false);
        Set<ConstraintViolation<OutUserDto>> nullIdViolations = validator.validate(nullIdDto);
        assertFalse(nullIdViolations.isEmpty());
        assertTrue(nullIdViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("id")));

        // Zu kurzer Username
        OutUserDto shortUsernameDto = new OutUserDto("3", "Al", "USER", "al@mail.com", true);
        Set<ConstraintViolation<OutUserDto>> shortUsernameViolations = validator.validate(shortUsernameDto);
        assertFalse(shortUsernameViolations.isEmpty());
        assertTrue(shortUsernameViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }
}