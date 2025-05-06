package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
public class RegisterUserDto extends LoginUserDto{
    @NotNull
    @Size(min = 3, max = 255)
    private String username;

    public RegisterUserDto(String email, String password, String userName) {
        super(email, password);
        this.username = userName;
    }

}