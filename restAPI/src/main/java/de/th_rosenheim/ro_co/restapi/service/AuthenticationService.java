package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.exceptions.NonUniqueException;
import de.th_rosenheim.ro_co.restapi.security.AuthenticationProviderConfiguration;
import de.th_rosenheim.ro_co.restapi.dto.*;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import de.th_rosenheim.ro_co.restapi.security.JwtService;
import de.th_rosenheim.ro_co.restapi.security.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.th_rosenheim.ro_co.restapi.security.JwtService.hashToken;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

private static final String EMAIL_REGEX = "^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$";
private static final String PWD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$";

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public Optional<OutUserDto> signup(RegisterUserDto input) throws NonUniqueException, IllegalArgumentException {
        User user = UserMapper.INSTANCE.registerUserDtotoUser(input);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new NonUniqueException("Email already exists");
        }

        //check if valide email format
        if (!user.getEmail().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        //check password strenght
        if (!user.getPassword().matches(PWD_REGEX)) {
            throw new IllegalArgumentException("Password must be 8 to 24 characters long, include uppercase and lowercase letters, a number, and a special character (!, @, #, $, %).");
        }

        user.setRole(Role.USER);
        user.setVerified(true); // default is false, set to true after email verification
        user.setPassword(AuthenticationProviderConfiguration.passwordEncoder().encode(input.getPassword()));
        User dbUser = userRepository.insert(user);

        OutUserDto response = UserMapper.INSTANCE.userToOutUserDto(user);
        response.setId(dbUser.getId());
        response.setVerified(true); // not jet implemented

        return Optional.of(response);
    }

    public Optional<LoginOutDto> authenticate(LoginUserDto input) throws AuthenticationException {

        // Try to authenticate the user via email and password throws AuthenticationException if not successful
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        // Check if the user exists and is verified
        Optional<User> user = userRepository.findByEmail(input.getEmail());
        if (user.isEmpty() || !user.get().isVerified()) {
            throw new UsernameNotFoundException("Invalid email or password");
        }
        String jwtToken = jwtService.generateToken(user.get());
        String refreshToken = jwtService.generateRefreshToken(user.get());

        OutUserDto outUserDTO = UserMapper.INSTANCE.userToOutUserDto(user.get());
        LoginOutDto response = new LoginOutDto(outUserDTO);
        response.setRefreshToken(refreshToken);
        response.setToken(jwtToken);
        response.setExpiresIn(jwtService.getExpirationTime());
        return Optional.of(response);
    }

    public Optional<LoginOutDto> refresh(String authHeader) throws UsernameNotFoundException {

        if (!jwtService.isBearer(authHeader)) {
            return Optional.empty();
        }
        //extract auth information from token
        String refreshTokenCandidate = jwtService.extractToken(authHeader);
        String username = jwtService.extractUsername(refreshTokenCandidate);
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isEmpty() || !user.get().isVerified()) {
            throw new UsernameNotFoundException("Invalid account");
        }

        if (!jwtService.isRefreshTokenValid(refreshTokenCandidate, user.get())) {
            return Optional.empty();
        }

        //check if the refresh token is still active
        boolean is_active_token = user.get().getRefreshTokens().stream().anyMatch(refreshToken ->
                hashToken(refreshTokenCandidate).equals(refreshToken.getToken_hash())
        );

        if (!is_active_token) {
            return Optional.empty();
        }

        String jwtToken = jwtService.generateToken(user.get());


        OutUserDto outUserDTO = UserMapper.INSTANCE.userToOutUserDto(user.get());
        LoginOutDto response = new LoginOutDto(outUserDTO);
        response.setToken(jwtToken);
        response.setRefreshToken(refreshTokenCandidate);
        response.setExpiresIn(jwtService.getExpirationTime());
        return Optional.of(response);
    }

    public boolean isDisplayNameAvailable(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new NonUniqueException("Username is required");
        }
        Long count = userRepository.countByDisplayName(displayName);
        return count == null || count <= 0;
    }
}
