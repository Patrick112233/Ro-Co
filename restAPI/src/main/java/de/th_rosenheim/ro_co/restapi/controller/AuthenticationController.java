package de.th_rosenheim.ro_co.restapi.controller;

import de.th_rosenheim.ro_co.restapi.dto.LoginResponse;
import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.RegisterUserDto;
import de.th_rosenheim.ro_co.restapi.dto.UserDTO;
import de.th_rosenheim.ro_co.restapi.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        Optional<LoginResponse> responseDTO = authenticationService.signup(registerUserDto);
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
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        Optional<LoginResponse> loginResponse = authenticationService.authenticate(loginUserDto);
        return loginResponse.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    //@todo: Add logout endpoint

    //@todo: Add refresh token endpoint

    //@todo: Add password reset endpoint

    //@todo: Add email verification endpoint

}