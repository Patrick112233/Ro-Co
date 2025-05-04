package de.th_rosenheim.ro_co.restapi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginOutDto extends OutUserDto {
    private String token;
    private String refreshToken;
    private long expiresIn;


    public LoginOutDto(OutUserDto user){
        super(user.getId(), user.getUsername(), user.getRole(), user.getEmail(), user.isVerified());
    }


}