package de.th_rosenheim.ro_co.restapi.model;

import de.th_rosenheim.ro_co.restapi.security.AuthenticationProviderConfig;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kong.unirest.core.GetRequest;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import lombok.*;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.net.URL;
import java.util.*;


@Data
@Document(collection="User")
public class User implements UserDetails {
    private static final Random RANDOM = new Random();
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
    private String password; //should be always encrypted!
    @NotNull
    @Size(min = 3, max = 255)
    private String displayName;
    private boolean verified = false; //default is false, set to true after email verification
    private String role;
    @Setter(AccessLevel.NONE)
    private Binary image = null; //size < 16MB
    @Setter(AccessLevel.NONE)
    private boolean hasImage = false;

    @DocumentReference(lazy = true)
    private transient List<RefreshToken> refreshTokens = new ArrayList<>(); //to small for hash list


    public static User instantiateUser(String email, String password,  String username, String role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setDisplayName(username);
        if (role == null) {
            user.setRole(Role.USER);
        }else {
            user.setRole(role);
        }
        return user;
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
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token cannot be null");
        }
        if (!refreshTokens.contains(refreshToken)) {
            throw new IllegalArgumentException("Refresh token not found in user's refresh tokens");
        }
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
        this.password = AuthenticationProviderConfig.passwordEncoder().encode(password);
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


    /**
     * Generates a user icon for the user. Overwrites the existing icon if it exists.
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void generateUserIcon() throws IOException, IllegalArgumentException {
        Binary svgBinary;
        byte[] svgBytes;
        try{
            svgBytes = this.generateAvatar(String.valueOf(System.currentTimeMillis()));
            this.hasImage = true;
        } catch (ValidationException e) {
            svgBytes = this.getDefaultAvatar();
        }
        svgBinary = new Binary(svgBytes);
        this.image = svgBinary;
    }

    private byte[] getDefaultAvatar() throws IOException {
        URL resource = getClass().getResource("default_avatar.svg");
        if (resource == null) {
            throw new IOException("Default avatar not found in resources");
        }
        try (var resourceStream = resource.openStream()) {
            return resourceStream.readAllBytes();
        }
    }

    private byte[] generateAvatar( String seed) throws ValidationException,IllegalArgumentException {
        if (this.getDisplayName() == null || this.getDisplayName().isEmpty() || seed == null) {
            throw new IllegalArgumentException("User or display name cannot be null or empty");
        }
        String[] colors = {"93ac23", "bd9c13", "65dd6c", "24795f", "98848f", "4b8acd", "810be6", "bdeadc", "897f40", "59a8d4", "b1537b","7a316f", "788760"};

        String randomColor1 = colors[RANDOM.nextInt(0, colors.length-1)];
        String randomColor2 = colors[RANDOM.nextInt(0, colors.length-1)];
        String iconUrl = "https://api.dicebear.com/9.x/bottts/svg?seed=" + this.getDisplayName() +
                "&backgroundColor="+randomColor1+","+randomColor2+"&backgroundType=gradientLinear";
        try {
            GetRequest request = Unirest.get(iconUrl);
            HttpResponse<String> response = request.asString();
            if (!response.getHeaders().get("Content-Type").contains("image/svg+xml")) {
                throw new ValidationException("Unexpected content type: " + response.getHeaders().get("Content-Type"));
            }
            //check if response is SVG
            if (response.getStatus() != 200) {
                throw new ValidationException("Failed to fetch user icon from Dicebear API. Response code: " + response.getStatus());
            }

            String responseBody = response.getBody();
            if (!responseBody.matches("^<svg.*?>.*?<\\/svg>$")) {
                throw new ValidationException("Response does not contain valid SVG data.");
            }
            return responseBody.getBytes();
        } catch (UnirestException e) {
            throw new ValidationException("Error fetching user icon: " + e.getMessage(), e);
        }
    }


}
