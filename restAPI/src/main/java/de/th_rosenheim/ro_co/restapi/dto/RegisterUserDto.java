package de.th_rosenheim.ro_co.restapi.dto;

public class RegisterUserDto extends LoginUserDto{
    private String firstName;
    private String lastName;

    public RegisterUserDto(String email, String password, String firstName, String lastName) {
        super(email, password);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public RegisterUserDto() {
        // Default constructor for deserialization
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
}