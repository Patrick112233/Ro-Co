package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.exceptions.NonUniqueException;
import de.th_rosenheim.ro_co.restapi.mapper.UserMapper;
import de.th_rosenheim.ro_co.restapi.dto.*;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import de.th_rosenheim.ro_co.restapi.model.Role;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.th_rosenheim.ro_co.restapi.mapper.Validator.validationCheck;
import static de.th_rosenheim.ro_co.restapi.service.JwtService.hashToken;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public Optional<OutUserDto> signup(@Valid RegisterUserDto input) throws NonUniqueException, IllegalArgumentException {
        User user = UserMapper.INSTANCE.registerUserDtotoUser(input);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new NonUniqueException("Email already exists");
        }

        user.setRole(Role.USER);
        user.setVerified(true); // default is false, set to true after email verification
        user.setEncPassword(input.getPassword());
        validationCheck(user);

        User dbUser = userRepository.insert(user);

        OutUserDto response = UserMapper.INSTANCE.userToOutUserDto(user);
        response.setId(dbUser.getId());
        response.setVerified(true); // not jet implemented

        return Optional.of(validationCheck(response));
    }

    public Optional<LoginOutDto> authenticate(@Valid LoginUserDto input) throws AuthenticationException {

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
        response.setTokenExpiresIn(jwtService.getTokenExpirationTime());
        response.setRefreshExpiresIn(jwtService.getRefreshTokenExpirationTime());
        return Optional.of(validationCheck(response));
    }

    public Optional<LoginOutDto> refresh(String authHeader) throws UsernameNotFoundException {

        if (!jwtService.isBearer(authHeader)) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        //extract auth information from token
        String refreshTokenCandidate = jwtService.extractToken(authHeader);
        String username = jwtService.extractUsername(refreshTokenCandidate);
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isEmpty() || !user.get().isVerified()) {
            throw new UsernameNotFoundException("Invalid account");
        }

        if (!jwtService.isRefreshTokenValid(refreshTokenCandidate, user.get())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        //check if the refresh token is still active
        boolean is_active_token = user.get().getRefreshTokens().stream().anyMatch(refreshToken ->
                hashToken(refreshTokenCandidate).equals(refreshToken.getTokenHash())
        );

        if (!is_active_token) {
            throw new ExpiredJwtException(null, null, "Refresh token is not known to the server");
        }

        String jwtToken = jwtService.generateToken(user.get());


        OutUserDto outUserDTO = UserMapper.INSTANCE.userToOutUserDto(user.get());
        LoginOutDto response = new LoginOutDto(outUserDTO);
        response.setToken(jwtToken);
        response.setRefreshToken(refreshTokenCandidate);
        response.setRefreshExpiresIn(jwtService.getRefreshTokenExpirationTime());
        response.setTokenExpiresIn(jwtService.getTokenExpirationTime());
        return Optional.of(validationCheck(response));
    }

    public boolean isDisplayNameAvailable(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new NonUniqueException("Username is required");
        }
        Long count = userRepository.countByDisplayName(displayName);
        return count == null || count <= 0;
    }
}
