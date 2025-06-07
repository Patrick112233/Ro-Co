package de.th_rosenheim.ro_co.restapi.mapper;

import de.th_rosenheim.ro_co.restapi.dto.InUserDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import de.th_rosenheim.ro_co.restapi.dto.RegisterUserDto;
import de.th_rosenheim.ro_co.restapi.model.Role;
import de.th_rosenheim.ro_co.restapi.model.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = UserMapper.INSTANCE;

    @Test
    void userToOutUserDto() {
        User user = new User("test@mail.com", "Pw123456!", "Max Mustermann", "USER");
        user.setId("507f1f77bcf86cd799439011");
        user.setVerified(true);

        OutUserDto dto = mapper.userToOutUserDto(user);

        assertEquals("507f1f77bcf86cd799439011", dto.getId());
        assertEquals("Max Mustermann", dto.getUsername());
        assertEquals("test@mail.com", dto.getEmail());
        assertEquals("USER", dto.getRole());
        assertTrue(dto.isVerified());

    }

    @Test
    void registerUserDtotoUser() {
        RegisterUserDto dto = new RegisterUserDto("not@mail.com", "Pw123456!", "John Doe");


        User user = mapper.registerUserDtotoUser(dto);

        assertNull(user.getId());
        assertEquals("John Doe", user.getDisplayName());
        assertEquals("not@mail.com", user.getEmail());
        assertEquals(Role.USER.getRole(),user.getRole());
        assertFalse(user.isVerified());


        //Test with invalid Username
        RegisterUserDto invalidDto = new RegisterUserDto("not@mail.com", "Pw123456!", "John Doe");
        Field field = null;
        try {
            field = RegisterUserDto.class.getDeclaredField("username");
            field.setAccessible(true);
            field.set(invalidDto, "Al");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        assertThrows(IllegalArgumentException.class, () -> mapper.registerUserDtotoUser(invalidDto));

        //check if PW is encrypted
        RegisterUserDto PwDto = new RegisterUserDto("not@mail.com", "Clear1234!", "John Doe");
        User Clear = mapper.registerUserDtotoUser(PwDto);
        assertNotEquals("Clear1234!", Clear.getPassword());

    }

}