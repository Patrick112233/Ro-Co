package de.th_rosenheim.ro_co.restapi.service;
import org.springframework.stereotype.Service;
import java.util.Optional;
import de.th_rosenheim.ro_co.restapi.model.User;

@Service
public class UserService {

    public Optional<User> getUser() {
        Optional<User> optional;
        User user = new User(5,"Eva",  "eva@mail.com");
        optional = Optional.of(user);
        return optional;
    }
}