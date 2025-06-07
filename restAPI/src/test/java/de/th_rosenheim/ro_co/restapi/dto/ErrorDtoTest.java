
package de.th_rosenheim.ro_co.restapi.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorDtoTest {

    @Test
    void testErrorMessageGetterAndSetter() {
        ErrorDto errorDto = new ErrorDto("Fehlermeldung");
        assertEquals("Fehlermeldung", errorDto.getErrorMessage());

        errorDto.setErrorMessage("Neue Fehlermeldung");
        assertEquals("Neue Fehlermeldung", errorDto.getErrorMessage());
    }
}