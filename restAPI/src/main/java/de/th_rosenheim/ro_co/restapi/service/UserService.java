package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.InUserDto;
import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import de.th_rosenheim.ro_co.restapi.mapper.UserMapper;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import de.th_rosenheim.ro_co.restapi.model.User;

import java.util.Optional;

import static de.th_rosenheim.ro_co.restapi.mapper.Validator.validationCheck;

//https://www.bezkoder.com/spring-boot-mongodb-pagination/

@Service
public class UserService {

    private UserRepository repository;

    UserService(UserRepository repository) {
        this.repository = repository;
    }



    public Optional<OutUserDto> getUser(String id) throws IllegalArgumentException, ValidationException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        Optional<User> user = repository.findById(id);

        //validate DTO bevore
        return user.map(u -> validationCheck(UserMapper.INSTANCE.userToOutUserDto(u)));
    }

    public Page<OutUserDto> getAllUsers(int page, int size) throws IllegalArgumentException, ValidationException {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size parameters.");
        }
        PageRequest pageRequest = PageRequest.of(page, size);

        return repository.findAll(pageRequest).map(user -> validationCheck(UserMapper.INSTANCE.userToOutUserDto(user)));
    }

    public OutUserDto updateUser(String id, InUserDto updatedUser)  throws IllegalArgumentException {
        if (id == null || !ObjectId.isValid(id)) {
            throw new IllegalArgumentException("ID is invalid");
        }
        if (updatedUser == null) {
            throw new IllegalArgumentException("Updated user data cannot be null");
        }

        // Check if the user exists before saving
        Optional<User> existingUser = repository.findById(id);
        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + id + " does not exist");
        }
        //update properties
        User newUser = existingUser.get();
        newUser.setDisplayName(updatedUser.getUsername());

        validationCheck(newUser);
        User dbUser = repository.save(newUser); //either creates or updates user
        return UserMapper.INSTANCE.userToOutUserDto(dbUser);
    }

    public void deleteUser(String id) throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        repository.deleteById(id);
    }

    public void resetPassword(String id,  LoginUserDto loginUserDto) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User with email " + loginUserDto.getEmail() + " not found");
        }
        user.get().setPassword(loginUserDto.getPassword());
        repository.save(user.get());
    }



}