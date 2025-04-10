package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterUserDto extends LoginUserDto{
    @NotNull
    @Size(min = 3, max = 255)
    private String username;

    public RegisterUserDto(String email, String password, String userName) {
        super(email, password);
        this.username = userName;
    }

    public RegisterUserDto() {
        // Default constructor for deserialization
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}