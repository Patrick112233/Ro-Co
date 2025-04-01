package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.ApplicationConfiguration;
import de.th_rosenheim.ro_co.restapi.dto.*;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import de.th_rosenheim.ro_co.restapi.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public Optional<LoginResponse> signup(RegisterUserDto input) {
        LoginResponse response = UserMapper.INSTANCE.registerUserDtoToLoginResponse(input);
        response.setRole(User.Role.USER.getRole());
        response.setVerified(true);//@TODO Add Mail verification -> set to false by default!
        User user = UserMapper.INSTANCE.inUserDtotoUser(response);
        user.setPassword(ApplicationConfiguration.passwordEncoder().encode(input.getPassword()));

        //@TODO: Add email verification

        User dbUser = userRepository.insert(user);

        response.setId(dbUser.getId());

        return Optional.of(response);
    }

    public Optional<LoginResponse> authenticate(LoginUserDto input) {

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
        UserDTO userDTO = UserMapper.INSTANCE.userToUserDto(user.get());
        LoginResponse response =  new LoginResponse(userDTO);
        response.setToken(jwtToken);
        response.setExpiresIn(jwtService.getExpirationTime());
        return Optional.of(response);
    }
}
