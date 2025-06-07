package de.th_rosenheim.ro_co.restapi.model;

import de.th_rosenheim.ro_co.restapi.security.AuthenticationProviderConfig;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;


@Data
@Document(collection="User")
public class User implements UserDetails {

    static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$";

    /**
     * The ID of the User.
     * This is an oid determined by MongoDB.
     * e.g. "5ff1e194b4f39b6e52a8314f".
     */
    @Id
    private String id;
    @NotNull
    @Pattern(
            regexp = "^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$",
            message = "Invalid email format"
    )
    @Indexed(unique=true, direction= IndexDirection.DESCENDING)
    private String email;
    @NotNull
    private String encPassword; //should be always encrypted!
    @NotNull
    @Size(min = 3, max = 255)
    private String displayName;
    private boolean verified = false; //default is false, set to true after email verification
    private String role;
    @DocumentReference(lazy = true)
    private transient List<RefreshToken> refreshTokens = new ArrayList<>(); //to small for hash list


    public User(String email, String password,  String displayName, String role) {
        setEmail(email);
        setPassword(password);
        setDisplayName(displayName);
        if (role == null) {
            this.role = Role.USER.toString();
        }else {
            setRole(role);
        }
    }


    public void setDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }
        if (displayName.length() < 3 || displayName.length() > 255) {
            throw new IllegalArgumentException("Display name must be between 3 and 255 characters");
        }
        this.displayName = displayName;
    }

    public void setId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        } else if (this.id != null) {
            throw new IllegalArgumentException("ID is already set");
        }
        if (!ObjectId.isValid(id)) {
            throw new IllegalArgumentException("ID is not a valid ObjectId");
        }
        this.id = id;
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


    public void addRefreshToken(RefreshToken refreshToken) {
        refreshTokens.add(refreshToken);
    }

    public void removeRefreshToken(RefreshToken refreshToken) {
        refreshTokens.remove(refreshToken);
    }

    public List<RefreshToken> getRefreshTokens() {
        return Collections.unmodifiableList(refreshTokens);
    }


    public void setEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!email.matches("^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$")) {
            throw new IllegalArgumentException("Email does not match the required pattern");
        }
        this.email = email;
    }

    public void setPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (!password.matches(PASSWORD_PATTERN)) {
            throw new IllegalArgumentException("Password does not match the required pattern");
        }
        this.encPassword = AuthenticationProviderConfig.passwordEncoder().encode(password);
    }

    @Override
    public String getPassword() {
        return encPassword;
    }

    /**
     * Gibt die E-Mail des Benutzers als Benutzernamen zurück. Nicht den Anzeigenamen.
     * @return die E-Mail des Benutzers
     */
    @Override
    public String getUsername() {
        return this.getEmail();
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
