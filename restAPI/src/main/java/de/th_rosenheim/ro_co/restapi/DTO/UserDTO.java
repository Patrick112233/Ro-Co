package de.th_rosenheim.ro_co.restapi.DTO;

import jakarta.validation.constraints.NotNull;

public class UserDTO {
    private String id;
    private String firstName;
    private String lastName;
    @NotNull
    private String email;


    public UserDTO(String id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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
}
