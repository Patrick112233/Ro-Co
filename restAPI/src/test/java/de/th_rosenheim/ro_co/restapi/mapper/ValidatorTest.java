package de.th_rosenheim.ro_co.restapi.mapper;

import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    void validationCheck_validOutUserDto() {
        OutUserDto dto = new OutUserDto("1", "Max", "USER", "max@mail.com", true);
        assertDoesNotThrow(() -> Validator.validationCheck(dto));
    }

    @Test
    void validationCheck_invalidEmailPattern() {
        OutUserDto dto = new OutUserDto("2", "Anna", "USER", "invalid-email", false);
        ValidationException ex = assertThrows(ValidationException.class, () -> Validator.validationCheck(dto));
        assertTrue(ex.getMessage().contains("Invalid email format"));
    }

    @Test
    void validationCheck_nullId() {
        OutUserDto dto = new OutUserDto(null, "Ben", "USER", "ben@mail.com", false);
        ValidationException ex = assertThrows(ValidationException.class, () -> Validator.validationCheck(dto));
        assertTrue(ex.getMessage().contains("id"));
    }
}