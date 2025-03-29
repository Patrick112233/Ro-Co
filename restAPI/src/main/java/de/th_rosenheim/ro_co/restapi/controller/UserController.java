package de.th_rosenheim.ro_co.restapi.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.th_rosenheim.ro_co.restapi.DTO.UserDTO;
import de.th_rosenheim.ro_co.restapi.DTO.UserMapper;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve the details of a user by their unique ID.")
    public ResponseEntity<UserDTO> getUser(@Valid @PathVariable String id) {
        Optional<UserDTO> user = userService.getUser(id);
        if (!user.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user.get());
    }

    @Operation(summary = "Create a new user", description = "Add a new user to the system by providing user details as JSON.")
    @PostMapping(value = "/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO inUserDTO) {
        UserDTO outUserDTO = userService.saveUser(inUserDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(outUserDTO.getId())
                .toUri();
        return ResponseEntity.created(uri).body(outUserDTO);
    }

/*
    @PutMapping("/user/{id}")
    @Operation(summary = "Update user by ID", description = "Update an existing user's details using their unique ID.")
    public ResponseEntity<User>  updateUser(@PathVariable long id, @RequestBody User updatedUser){
        User user = this.userService.updateUser(id,updatedUser);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(uri).body(user);
    }


    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve a paginated list of users based on the provided page and size.")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size parameters.");
        }
        Page<User> users = this.userService.getAllUsers(page, size);
        return ResponseEntity.ok(users.getContent());
    }

    @DeleteMapping("/user/{id}")
    @Operation(summary = "Delete user by ID", description = "Remove a user from the system by their unique ID.")
    public ResponseEntity<Object> deleteUser(@PathVariable long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }*/
}