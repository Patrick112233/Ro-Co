package de.th_rosenheim.ro_co.restapi.dto;

public class LoginResponseDto extends OutUserDto {
    private String token;
    private long expiresIn;

    public LoginResponseDto() {
        // Default constructor for deserialization
    }
    public LoginResponseDto(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public LoginResponseDto(OutUserDto user){
        super(user.getId(), user.getFirstName(), user.getLastName(), user.getRole(), user.getEmail(), user.isVerified());
    }


    public String getToken() {
        return token;
    }
    public LoginResponseDto setToken(String token) {
        this.token = token;
        return this;
    }
    public long getExpiresIn() {
        return expiresIn;
    }
    public LoginResponseDto setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + token + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }


}