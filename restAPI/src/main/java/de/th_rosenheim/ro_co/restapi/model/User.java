package de.th_rosenheim.ro_co.restapi.model;

import lombok.*;
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
    private String displayName;
    private boolean verified = false; //default is false, set to true after email verification
    private String role;

    @DocumentReference(lazy = true)
    private List<RefreshToken> refreshTokens;


    // Getter und Setter für id
    public void setId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        } else if (this.id != null) {
            throw new IllegalArgumentException("ID is already set");
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

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Gibt die E-Mail des Benutzers als Benutzernamen zurück. Nicht den Anzeigenamen.
     * @return die E-Mail des Benutzers
     */
    @Override
    public String getUsername() {
        return this.getEmail();
    }



    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        if (role == null) {
            this.role = Role.USER.toString();
        }else {
            this.role = Role.fromString(role).getRole();
        }
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


    public void addRefreshToken(RefreshToken refreshToken) {
        if (refreshTokens == null) {
            refreshTokens = new ArrayList<>();
        }
        refreshTokens.add(refreshToken);
    }
}
