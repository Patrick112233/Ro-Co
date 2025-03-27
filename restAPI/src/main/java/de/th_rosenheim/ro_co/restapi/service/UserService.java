package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import de.th_rosenheim.ro_co.restapi.model.User;

//https://www.bezkoder.com/spring-boot-mongodb-pagination/

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public User getUser(String id) {
        //manage Optional
        //Manage DTO!
        return repository.findById(id).get();
    }

    public Page<User> getAllUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return repository.findAll(pageRequest);
    }

    public User saveUser(User user) {
        return repository.insert(user);
    }

    public User updateUser(String id, User updatedUser) {
        if (!updatedUser.getId().equals(id)) {
            throw new IllegalArgumentException("Invalid user ID.");
        }
        return repository.save(updatedUser);
    }

    public void deleteUser(String id) {
        repository.deleteById(id);
    }

}