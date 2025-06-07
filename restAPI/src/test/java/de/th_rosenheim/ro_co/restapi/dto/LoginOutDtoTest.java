package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginOutDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void allLoginOutDtoValidationCases() {
        // Gültiger Fall
        LoginOutDto validDto = new LoginOutDto(
                "token123", "refresh456", 3600L, 7200L
        );
        validDto.setId("1");
        validDto.setUsername("ValidUser");
        validDto.setRole("USER");
        validDto.setEmail("valid@mail.com");
        validDto.setVerified(true);

        Set<ConstraintViolation<LoginOutDto>> validViolations = validator.validate(validDto);
        assertTrue(validViolations.isEmpty(), "Es sollten keine Validierungsfehler auftreten.");

        // Null-Token
        LoginOutDto nullTokenDto = new LoginOutDto(
                null, "refresh456", 3600L, 7200L
        );
        nullTokenDto.setId("1");
        nullTokenDto.setUsername("ValidUser");
        nullTokenDto.setRole("USER");
        nullTokenDto.setEmail("valid@mail.com");
        nullTokenDto.setVerified(true);

        Set<ConstraintViolation<LoginOutDto>> nullTokenViolations = validator.validate(nullTokenDto);
        assertFalse(nullTokenViolations.isEmpty());
        assertTrue(nullTokenViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("token")));

        // Null-RefreshToken
        LoginOutDto nullRefreshTokenDto = new LoginOutDto(
                "token123", null, 3600L, 7200L
        );
        nullRefreshTokenDto.setId("1");
        nullRefreshTokenDto.setUsername("ValidUser");
        nullRefreshTokenDto.setRole("USER");
        nullRefreshTokenDto.setEmail("valid@mail.com");
        nullRefreshTokenDto.setVerified(true);

        Set<ConstraintViolation<LoginOutDto>> nullRefreshTokenViolations = validator.validate(nullRefreshTokenDto);
        assertFalse(nullRefreshTokenViolations.isEmpty());
        assertTrue(nullRefreshTokenViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("refreshToken")));

        // Negativer tokenExpiresIn
        LoginOutDto negativeTokenExpiresDto = new LoginOutDto(
                "token123", "refresh456", -1L, 7200L
        );
        negativeTokenExpiresDto.setId("1");
        negativeTokenExpiresDto.setUsername("ValidUser");
        negativeTokenExpiresDto.setRole("USER");
        negativeTokenExpiresDto.setEmail("valid@mail.com");
        negativeTokenExpiresDto.setVerified(true);

        validator.validate(negativeTokenExpiresDto);

        // Negativer refreshExpiresIn
        LoginOutDto negativeRefreshExpiresDto = new LoginOutDto(
                "token123", "refresh456", 3600L, -1L
        );
        negativeRefreshExpiresDto.setId("1");
        negativeRefreshExpiresDto.setUsername("ValidUser");
        negativeRefreshExpiresDto.setRole("USER");
        negativeRefreshExpiresDto.setEmail("valid@mail.com");
        negativeRefreshExpiresDto.setVerified(true);

        validator.validate(negativeRefreshExpiresDto);
    }
}