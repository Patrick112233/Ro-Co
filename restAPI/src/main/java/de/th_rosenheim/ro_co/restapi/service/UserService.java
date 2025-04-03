package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.InUserDto;
import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import de.th_rosenheim.ro_co.restapi.dto.UserMapper;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import de.th_rosenheim.ro_co.restapi.security.AuthenticationProviderConfiguration;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import de.th_rosenheim.ro_co.restapi.model.User;

import java.util.Optional;

//https://www.bezkoder.com/spring-boot-mongodb-pagination/

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public Optional<OutUserDto> getUser(String id) throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        Optional<User> user = repository.findById(id);
        return user.map(UserMapper.INSTANCE::userToOutUserDto);
    }

    public Page<OutUserDto> getAllUsers(int page, int size) throws IllegalArgumentException {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size parameters.");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        return repository.findAll(pageRequest).map(UserMapper.INSTANCE::userToOutUserDto);
    }

    public OutUserDto updateUser(String id, InUserDto updatedUser)  throws IllegalArgumentException {
        if (id == null || !ObjectId.isValid(id)) {
            throw new IllegalArgumentException("ID is invalid");
        }
        User inUser = UserMapper.INSTANCE.inUserDtotoUser(updatedUser);
        inUser.setId(id); //force overwrite id

        // Check if the user exists before saving
        Optional<User> existingUser = repository.findById(id);
        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + id + " does not exist");
        }
        //ensure some values are not changed by api


        User dbUser = repository.save(inUser); //either creates or updates user
        return UserMapper.INSTANCE.userToOutUserDto(dbUser);
    }

    public void deleteUser(String id) throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        repository.deleteById(id);
    }

    public void resetPassword(@Valid LoginUserDto loginUserDto) {
        Optional<User> user = repository.findByEmail(loginUserDto.getEmail());
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User with email " + loginUserDto.getEmail() + " not found");
        }
        user.get().setPassword(AuthenticationProviderConfiguration.passwordEncoder().encode(loginUserDto.getPassword()));
        repository.save(user.get());
    }
}