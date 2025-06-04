package de.th_rosenheim.ro_co.restapi.exceptions;

import de.th_rosenheim.ro_co.restapi.dto.ErrorDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.View;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final View error;


    public GlobalExceptionHandler(View error) {
        this.error = error;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        ErrorDto errorDto = new ErrorDto("Bad Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }
}