package de.th_rosenheim.ro_co.restapi.service;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import de.th_rosenheim.ro_co.restapi.model.User;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public Optional<User> getUser(int id) {
        Optional<User> optional;
        User user = repository.findById(id); //= new User(5,"Eva",  "eva@mail.com");
        optional = Optional.of(user);
        return optional;
    }
}