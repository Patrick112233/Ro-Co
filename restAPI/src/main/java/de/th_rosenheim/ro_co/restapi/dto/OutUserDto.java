package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutUserDto {
    @NotNull
    private String id;

    @NotNull
    @Size(min = 3, max = 255)
    private String username;
    private String role;

    @NotNull
    @Pattern(
            regexp = "^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$",
            message = "Invalid email format"
    )
    private String email;
    private boolean verified;
}
