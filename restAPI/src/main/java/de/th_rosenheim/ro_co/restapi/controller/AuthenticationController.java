package de.th_rosenheim.ro_co.restapi.controller;

import com.mongodb.MongoException;
import de.th_rosenheim.ro_co.restapi.dto.ErrorDTO;
import de.th_rosenheim.ro_co.restapi.dto.LoginResponseDto;
import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.RegisterUserDto;
import de.th_rosenheim.ro_co.restapi.exceptions.NonUniqueException;
import de.th_rosenheim.ro_co.restapi.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.tomcat.websocket.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController( AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    @Operation(summary = "Create a new user", description = "Add a new user to the system by providing user details as JSON.")
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        Optional<LoginResponseDto> responseDTO = authenticationService.signup(registerUserDto);
        if (responseDTO.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        URI uri = UriComponentsBuilder
                .fromPath("/user/{id}")
                .buildAndExpand(responseDTO.get().getId())
                .toUri();
        return ResponseEntity.created(uri).body(responseDTO.get());
    }


    @Operation(summary = "Login as a user", description = "Get a JWT token by providing user credentials as JSON.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        Optional<LoginResponseDto> loginResponse = authenticationService.authenticate(loginUserDto);
        return loginResponse.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }


    @Operation(summary = "Refresh token", description = "Refresh the JWT token by providing the old (but valid) token.")
    @PutMapping("/login")
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestHeader(name="Authorization") String token) {
        Optional<LoginResponseDto> loginResponse = authenticationService.refresh(token);
        return loginResponse.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }


    @ExceptionHandler({
            UsernameNotFoundException.class,
            AuthenticationException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<Object> handleValidationExceptions(Exception ex) {
        ErrorDTO error = new ErrorDTO("The credentials entered are incorrect or the account has been disabled.");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorDTO error = new ErrorDTO("The request body is not readable or is missing required fields.");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NonUniqueException.class)
    public ResponseEntity<Object> handleNonUniqueException(NonUniqueException ex) {
        ErrorDTO error = new ErrorDTO("The email address is already in use.");
        return ResponseEntity.badRequest().body(error);
    }


}