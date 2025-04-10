package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;

public class OutUserDto {
    private String id;
    @NotNull
    private String username;
    private String role;
    @NotNull
    private String email;
    private boolean verified;


    public OutUserDto(String id, String firstName, String role, String email, boolean verified) {
        this.id = id;
        this.username = firstName;
        this.role = role;
        this.email = email;
        this.verified = verified;
    }

    /**
     * Default constructor for UserDTO.
     * This constructor is used by the Mapping framework to create instances of UserDTO.
     */
    public OutUserDto() {}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
