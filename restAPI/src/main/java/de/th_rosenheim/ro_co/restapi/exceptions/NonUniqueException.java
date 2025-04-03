package de.th_rosenheim.ro_co.restapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.CONFLICT, reason="No such Order")  // 404
public class NonUniqueException extends RuntimeException {
    public NonUniqueException(String emailAlreadyExists) {
        super(emailAlreadyExists);
    }
}
