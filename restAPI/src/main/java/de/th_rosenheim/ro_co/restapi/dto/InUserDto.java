package de.th_rosenheim.ro_co.restapi.dto;


public class InUserDto {
    private String firstName;
    private String lastName;

    public InUserDto(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Default constructor for UserDTO.
     * This constructor is used by the Mapping framework to create instances of UserDTO.
     */
    public InUserDto() {}


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


}
