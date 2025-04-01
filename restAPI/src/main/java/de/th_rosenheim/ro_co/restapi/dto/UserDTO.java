package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;

public class UserDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String role;
    @NotNull
    private String email;
    private boolean verified;


    public UserDTO(String id, String firstName, String lastName, String role, String email, boolean verified) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.email = email;
        this.verified = verified;
    }

    /**
     * Default constructor for UserDTO.
     * This constructor is used by the Mapping framework to create instances of UserDTO.
     */
    public UserDTO() {}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
