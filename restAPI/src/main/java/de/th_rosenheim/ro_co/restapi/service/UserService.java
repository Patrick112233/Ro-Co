package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.UserDTO;
import de.th_rosenheim.ro_co.restapi.dto.UserMapper;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Optional<UserDTO> getUser(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        Optional<User> user = repository.findById(id);
        return user.map(UserMapper.INSTANCE::userToUserDto);
    }

    public Page<UserDTO> getAllUsers(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size parameters.");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        return repository.findAll(pageRequest).map(UserMapper.INSTANCE::userToUserDto);
    }

    public UserDTO updateUser(String id, UserDTO updatedUser) {
        if (id == null || !ObjectId.isValid(id)) {
            throw new IllegalArgumentException("ID is invalid");
        }
        User inUser = UserMapper.INSTANCE.inUserDtotoUser(updatedUser);
        inUser.setId(id); //force overwrite id
        User dbUser = repository.save(inUser); //either creates or updates user
        return UserMapper.INSTANCE.userToUserDto(dbUser);
    }

    public void deleteUser(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        repository.deleteById(id);
    }

}