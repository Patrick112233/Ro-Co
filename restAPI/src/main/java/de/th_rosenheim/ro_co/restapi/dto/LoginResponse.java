package de.th_rosenheim.ro_co.restapi.dto;

public class LoginResponse extends UserDTO{
    private String token;
    private long expiresIn;

    public LoginResponse() {
        // Default constructor for deserialization
    }
    public LoginResponse(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public LoginResponse(UserDTO user){
        super(user.getId(), user.getFirstName(), user.getLastName(), user.getRole(), user.getEmail(), user.isVerified());
    }


    public String getToken() {
        return token;
    }
    public LoginResponse setToken(String token) {
        this.token = token;
        return this;
    }
    public long getExpiresIn() {
        return expiresIn;
    }
    public LoginResponse setExpiresIn(long expiresIn) {
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