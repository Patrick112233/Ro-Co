package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;

@Service
public class MongoAuthUserDetailService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private jakarta.validation.Validator validator;

    public MongoAuthUserDetailService(UserRepository userRepository, jakarta.validation.Validator validator) {
        this.userRepository = userRepository;
        this.validator = validator;
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

        Set<ConstraintViolation<User>> violations = validator.validate(user.get());
        if (!violations.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }
        return user.get();
    }

}