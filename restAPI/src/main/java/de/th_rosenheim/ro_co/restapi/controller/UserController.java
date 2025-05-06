package de.th_rosenheim.ro_co.restapi.controller;

import de.th_rosenheim.ro_co.restapi.dto.ErrorDto;
import de.th_rosenheim.ro_co.restapi.dto.InUserDto;
import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import de.th_rosenheim.ro_co.restapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RequestMapping("api/v1/user")
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user by ID", description = "Retrieve the details of a user by their unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<OutUserDto> getUser(@PathVariable String id) {
        Optional<OutUserDto> user = userService.getUser(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update user by ID", description = "Update an existing user's details using their unique ID.")
    @PutMapping("/{id}")
    public ResponseEntity<OutUserDto> updateUser(@PathVariable String id, @Valid @RequestBody InUserDto updatedUser){
        OutUserDto outUserDTO = this.userService.updateUser(id,updatedUser);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(outUserDTO.getId())
                .toUri();
        return ResponseEntity.created(uri).body(outUserDTO);
    }

    @Operation(summary = "Create a new user", description = "Add a new user to the system by providing user details as JSON.")
    @PutMapping("/{id}/password")
    public ResponseEntity<Object> resetPassword(@PathVariable String id, @Valid @RequestBody LoginUserDto loginUserDto) throws UsernameNotFoundException {
        this.userService.resetPassword(loginUserDto);
        return ResponseEntity.ok().build();
    }

    @Secured("ADMIN")
    @Operation(summary = "Get all users", description = "Retrieve a paginated list of users based on the provided page and size. Max size is 100.")
    @GetMapping("/all")
    public ResponseEntity<List<OutUserDto>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<OutUserDto> users = this.userService.getAllUsers(page, size);
        return ResponseEntity.ok(users.getContent());
    }

    @Secured("ADMIN")
    @Operation(summary = "Delete user by ID", description = "Remove a user from the system by their unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable String id) {
        this.userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }





    @ExceptionHandler({
            UsernameNotFoundException.class
    })
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        ErrorDto error = new ErrorDto("The credentials entered are incorrect or the account has been disabled.");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
        org.springframework.web.bind.MethodArgumentNotValidException.class
    })
    public ResponseEntity<Object> handleValidationExceptions(Exception ex) {
        ErrorDto error = new ErrorDto("The request body is not readable or is missing required fields or invalid inputs.");
        return ResponseEntity.badRequest().body(error);
    }

}