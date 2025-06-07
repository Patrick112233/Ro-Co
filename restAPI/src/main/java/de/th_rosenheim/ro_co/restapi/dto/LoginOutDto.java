package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginOutDto extends OutUserDto {
    @NotNull
    private String token;
    @NotNull
    private String refreshToken;
    @NotNull
    private long tokenExpiresIn;
    @NotNull
    private long refreshExpiresIn;


    public LoginOutDto(OutUserDto user){
        super(user.getId(), user.getUsername(), user.getRole(), user.getEmail(), user.isVerified());
    }


}