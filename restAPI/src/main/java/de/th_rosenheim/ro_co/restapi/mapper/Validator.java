package de.th_rosenheim.ro_co.restapi.mapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;


import java.util.Set;

public class Validator {
    private Validator() {
        // Utility class
    }

    public static <T> T validationCheck(T object) {
        Set<ConstraintViolation<T>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(object);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            for (ConstraintViolation<T> violation : violations) {
                errorMessage.append(violation.getPropertyPath())
                        .append(" ")
                        .append(violation.getMessage())
                        .append("; ");
            }
            throw new ValidationException(errorMessage.toString());
        }
        return object;
    }
}
