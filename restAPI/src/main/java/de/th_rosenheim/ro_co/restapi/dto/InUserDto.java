package de.th_rosenheim.ro_co.restapi.dto;


public class InUserDto {
    private String username;

    public InUserDto(String firstName) {
        this.username = firstName;
    }

    /**
     * Default constructor for UserDTO.
     * This constructor is used by the Mapping framework to create instances of UserDTO.
     */
    public InUserDto() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
