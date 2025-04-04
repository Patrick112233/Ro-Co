package de.th_rosenheim.ro_co.frontend.data;
import java.util.Arrays;
import java.util.Set;

public class User {

    public User(String username, String name, String hashedPassword, Set<Role> roles, byte[] profilePicture) {
        this.username = username;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.roles = roles;
        this.profilePicture = profilePicture;
    }

    private String username;
    private String name;
    private String hashedPassword;
    private Set<Role> roles;
    private byte[] profilePicture;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public byte[] getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }


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
}
