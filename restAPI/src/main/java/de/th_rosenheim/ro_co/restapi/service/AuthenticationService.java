package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.exceptions.NonUniqueException;
import de.th_rosenheim.ro_co.restapi.security.AuthenticationProviderConfiguration;
import de.th_rosenheim.ro_co.restapi.dto.*;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import de.th_rosenheim.ro_co.restapi.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
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

    public Optional<LoginResponseDto> signup(RegisterUserDto input) throws NonUniqueException {
        LoginResponseDto response = UserMapper.INSTANCE.registerUserDtoToLoginResponse(input);
        response.setRole(User.Role.USER.getRole());
        response.setVerified(true); // this filed is intendet for an Mail verification process that is not implemented yet
        User user = UserMapper.INSTANCE.outUserDTOtoUser(response);
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new NonUniqueException("Email already exists");
        }
        user.setPassword(AuthenticationProviderConfiguration.passwordEncoder().encode(input.getPassword()));
        User dbUser = userRepository.insert(user);
        response.setId(dbUser.getId());
        return Optional.of(response);
    }

    public Optional<LoginResponseDto> authenticate(LoginUserDto input) throws UsernameNotFoundException, AuthenticationException {

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
        OutUserDto outUserDTO = UserMapper.INSTANCE.userToOutUserDto(user.get());
        LoginResponseDto response =  new LoginResponseDto(outUserDTO);
        response.setToken(jwtToken);
        response.setExpiresIn(jwtService.getExpirationTime());
        return Optional.of(response);
    }

    public Optional<LoginResponseDto> refresh(String bearer) throws  UsernameNotFoundException{
        String token = jwtService.extractToken(bearer);
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isEmpty() || !user.get().isVerified()) {
            throw new UsernameNotFoundException("Invalid account");
        }

        if(!jwtService.isTokenValid(token, user.get())){
            return Optional.empty();
        }

        String jwtToken = jwtService.generateToken(user.get());
        OutUserDto outUserDTO = UserMapper.INSTANCE.userToOutUserDto(user.get());
        LoginResponseDto response =  new LoginResponseDto(outUserDTO);
        response.setToken(jwtToken);
        response.setExpiresIn(jwtService.getExpirationTime());
        return Optional.of(response);
    }
}
