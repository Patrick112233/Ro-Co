
package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InUserDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void allInUserDtoValidationCases() {
        // Gültiger Fall
        InUserDto validDto = new InUserDto("ValidUser");
        Set<ConstraintViolation<InUserDto>> validViolations = validator.validate(validDto);
        assertTrue(validViolations.isEmpty(), "Es sollten keine Validierungsfehler auftreten.");

        // Zu kurzer Username
        InUserDto shortUsernameDto = new InUserDto("Al");
        Set<ConstraintViolation<InUserDto>> shortUsernameViolations = validator.validate(shortUsernameDto);
        assertFalse(shortUsernameViolations.isEmpty());
        assertTrue(shortUsernameViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));

        // Null Username
        InUserDto nullUsernameDto = new InUserDto(null);
        Set<ConstraintViolation<InUserDto>> nullUsernameViolations = validator.validate(nullUsernameDto);
        assertFalse(nullUsernameViolations.isEmpty());
        assertTrue(nullUsernameViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }
}