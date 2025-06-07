package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

import static de.th_rosenheim.ro_co.restapi.mapper.Validator.validationCheck;

@Service
public class MongoAuthUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;


    public MongoAuthUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email cannot be null or empty");
        }
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }
        try {
            validationCheck(user.get());
        } catch (ValidationException e) {
            throw new UsernameNotFoundException("User validation failed for email: " + email);
        }
        return user.get();
    }

}