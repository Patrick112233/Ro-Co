package de.th_rosenheim.ro_co.restapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="User")
public class User{

    /**
     * The ID of the User.
     * This is an oid determined by MongoDB.
     * e.g. "5ff1e194b4f39b6e52a8314f".
     */
    @Indexed(unique=true, direction= IndexDirection.DESCENDING)
    @Id
    private String id;
    private String firstName;
    private String lastName;

    // Getter und Setter für id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter und Setter für firstName
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Getter und Setter für lastName
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // AllArgsConstructor
    public User(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // NoArgsConstructor
    public User() {
    }

    @Override
    public String toString() {
        return "User[" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ']';
    }
}
