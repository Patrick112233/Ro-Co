package de.th_rosenheim.ro_co.restapi.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.th_rosenheim.ro_co.restapi.DTO.UserDTO;
import de.th_rosenheim.ro_co.restapi.DTO.UserMapper;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

    @Operation(summary = "Get user by ID", description = "Retrieve the details of a user by their unique ID.")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUser(@Valid @PathVariable String id) {
        Optional<UserDTO> user = userService.getUser(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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

    @Operation(summary = "Update user by ID", description = "Update an existing user's details using their unique ID.")
    @PutMapping("/user/{id}")
    public ResponseEntity<UserDTO>  updateUser(@PathVariable String id, @Valid @RequestBody UserDTO updatedUser){
        UserDTO outUserDTO = this.userService.updateUser(id,updatedUser);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(outUserDTO.getId())
                .toUri();
        return ResponseEntity.created(uri).body(outUserDTO);
    }

    @Operation(summary = "Get all users", description = "Retrieve a paginated list of users based on the provided page and size. Max size is 100.")
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<UserDTO> users = this.userService.getAllUsers(page, size);
        return ResponseEntity.ok(users.getContent());
    }

    @Operation(summary = "Delete user by ID", description = "Remove a user from the system by their unique ID.")
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable String id) {
        this.userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}