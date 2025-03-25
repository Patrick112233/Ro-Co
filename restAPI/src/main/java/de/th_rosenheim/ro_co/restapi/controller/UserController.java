package de.th_rosenheim.ro_co.restapi.controller;

import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

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
    public User getUser(@PathVariable long id) {
        Optional<User> user = userService.getUser(id);
        return user.orElse(null);
    }


    @PostMapping("/user")
    @Operation(summary = "Create a new user", description = "Add a new user to the system by providing user details as JSON.")
    public User createUser(@RequestBody User newUser) {
        return this.userService.saveUser(newUser);
    }


    @PutMapping("/user/{id}")
    @Operation(summary = "Update user by ID", description = "Update an existing user's details using their unique ID.")
    public User updateUser(@PathVariable long id, @RequestBody User updatedUser){
        return this.userService.updateUser(id, updatedUser);
    }


    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve a paginated list of users based on the provided page and size.")
    public List<User> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size parameters.");
        }
        Page<User> users = this.userService.getAllUsers(page, size);
        return users.getContent();
    }

    @DeleteMapping("/user/{id}")
    @Operation(summary = "Delete user by ID", description = "Remove a user from the system by their unique ID.")
    public void deleteUser(@PathVariable long id) {
        this.userService.deleteUser(id);
    }
}