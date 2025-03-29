package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.DTO.UserDTO;
import de.th_rosenheim.ro_co.restapi.DTO.UserMapper;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import de.th_rosenheim.ro_co.restapi.model.User;

import java.util.Optional;
import java.util.UUID;

//https://www.bezkoder.com/spring-boot-mongodb-pagination/

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public Optional<UserDTO> getUser(String id) {
        Optional<User> user =  repository.findById(id);
        if (!user.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(UserMapper.INSTANCE.userToUserDto(user.get()));
    }

    public Page<User> getAllUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return repository.findAll(pageRequest);
    }

    public UserDTO saveUser(UserDTO inUserDto) {
        User inUser = UserMapper.INSTANCE.inUserDtotoUser(inUserDto);
        inUser.setId(UUID.randomUUID().toString());
        User dbUser = repository.insert(inUser);
        return UserMapper.INSTANCE.userToUserDto(dbUser);
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