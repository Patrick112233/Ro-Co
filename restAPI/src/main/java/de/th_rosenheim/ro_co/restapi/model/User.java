package de.th_rosenheim.ro_co.restapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;


@Document(collection="User")
public class User implements UserDetails {


    //define internal role enum(sTring)

    public enum Role {
        EXTERNAL_USER("EXTERNAL_USER"),
        USER("USER"),
        ADMIN("ADMIN");

        private final String title;

        Role(String role) {
            this.title = role;
        }

        public String getRole() {
            return title;
        }

        public static Role fromString(String role) {
            return Arrays.stream(Role.values())
                    .filter(r -> r.getRole().equals(role))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + role));
        }
    }



    /**
     * The ID of the User.
     * This is an oid determined by MongoDB.
     * e.g. "5ff1e194b4f39b6e52a8314f".
     */
    @Id
    private String id;

    @Indexed(unique=true, direction= IndexDirection.DESCENDING)
    private String email;

    private String password; //should be always encrypted!

    private String firstName;
    private String lastName;
    private boolean verified = false; //default is false, set to true after email verification
    private String role;

    // Getter und Setter für id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        } else if (this.id != null) {
            throw new IllegalArgumentException("ID is already set");
        }
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


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.getRole() == null) {
            return List.of();
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(this.getRole());
        authorities.add(authority);
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // AllArgsConstructor
    public User(String id, String firstName, String lastName, String email, String password, boolean verrified, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.verified = verrified;
        this.role = role;
    }

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role.getRole();
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


    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getRole() {
        return role;
    }

    public Role getRoleEnum() {
        if (role == null) {
            return null;
        }
        return Role.fromString(role);
    }

    public void setRole(String role) {
        //check if roll is in enum
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        if (Arrays.stream(Role.values()).noneMatch(r -> r.getRole().equals(role))) {
            throw new IllegalArgumentException("Role is not valid");
        }
        this.role = role;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = role.getRole();
    }
}
