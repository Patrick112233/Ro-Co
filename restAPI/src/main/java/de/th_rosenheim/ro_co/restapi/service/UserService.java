package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import de.th_rosenheim.ro_co.restapi.model.User;

//https://www.bezkoder.com/spring-boot-mongodb-pagination/

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public Optional<User> getUser(long id) {
        return Optional.ofNullable(repository.findById(id));
    }

    public Page<User> getAllUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return repository.findAll(pageRequest);
    }

    public User saveUser(User user) {
        return repository.insert(user);
    }

    public User updateUser(long id, User updatedUser) {
        User newUser;
        if ( updatedUser.getId() != id) {
            throw new IllegalArgumentException("Invalid user ID.");
        }
        return repository.save(updatedUser);

    }

}