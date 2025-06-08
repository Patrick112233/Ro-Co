package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.InUserDto;
import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static de.th_rosenheim.ro_co.restapi.model.User.instantiateUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository repository;
    private UserService userService;


    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        userService = new UserService(repository);
        // Feld per Reflection setzen, da @Autowired nicht greift
        java.lang.reflect.Field repoField;
        try {
            repoField = UserService.class.getDeclaredField("repository");
            repoField.setAccessible(true);
            repoField.set(userService, repository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void getUser() {

        // Teste gültige User
        String validId = "507f1f77bcf86cd799439011";
        User validUser = instantiateUser("valid@example.com", "Pw123456!", "Valid User","USER");
        validUser.setId(validId);
        validUser.setVerified(true);
        when(repository.findById(validId)).thenReturn(Optional.of(validUser));
        Optional<OutUserDto> result = userService.getUser(validId);
        assertTrue(result.isPresent());
        assertEquals("Valid User", result.get().getUsername());
        assertEquals("valid@example.com", result.get().getEmail());

        // Teste input Validierung
        assertThrows(IllegalArgumentException.class, () -> userService.getUser(null));
        assertThrows(IllegalArgumentException.class, () -> userService.getUser(""));
        assertDoesNotThrow(() -> userService.getUser("!@#$%^&*()"));

        //test with invalid fields in data:
        var invalidUser = instantiateUser("Not@mail.com", "Pw123456!","myName", "USER");
        try {
            Field field = User.class.getDeclaredField("email");
            field.setAccessible(true);
            field.set(invalidUser, "not.valid.email");

            Field passwordField = User.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(invalidUser, "pw");

            Field displayNameField = User.class.getDeclaredField("displayName");
            displayNameField.setAccessible(true);
            displayNameField.set(invalidUser, null);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        invalidUser.setVerified(false);
        when(repository.findById(validId)).thenReturn(Optional.of(invalidUser));
        // Da OutUserDto.email @NotNull ist, sollte eine Exception geworfen werden
        assertThrows(ValidationException.class, () -> userService.getUser(validId));
    }

    @Test
    void getAllUsers() {
        // Teste gültige User
        User user1 = instantiateUser("user1@example.com", "Password1234!", "User One","USER");
        user1.setVerified(true);
        user1.setId("507f1f77bcf86cd799439011");

        User user2 = instantiateUser("user2@example.com", "Password567!","User Two", "USER");
        user2.setVerified(true);
        user2.setId("507f1f77bcf86cd799439012");

        User user3 = instantiateUser("user3@example.com", "Password890!", "User Three","USER");
        user3.setVerified(true);
        user3.setId("507f1f77bcf86cd799439013");

        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(user1, user2, user3)));

        Page<OutUserDto> result = userService.getAllUsers(0, 10);
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals("User One", result.getContent().get(0).getUsername());
        assertEquals("User Two", result.getContent().get(1).getUsername());
        assertEquals("User Three", result.getContent().get(2).getUsername());


        // Ungültige page/size Kombinationen
        assertThrows(IllegalArgumentException.class, () -> userService.getAllUsers(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> userService.getAllUsers(0, 0));
        assertThrows(IllegalArgumentException.class, () -> userService.getAllUsers(0, 101));

        // User mit ungültiger E-Mail (Regex)
        var userInvalidEmail = instantiateUser("Not@mail.com", "Pw123456!","validName", "USER");
        try {
            Field field = User.class.getDeclaredField("email");
            field.setAccessible(true);
            field.set(userInvalidEmail, "not.valid.email");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        userInvalidEmail.setVerified(true);
        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(userInvalidEmail)));
        assertThrows(ValidationException.class, () -> userService.getAllUsers(0, 10));

        // User mit null-Werten in id, username, email
        var userWithNulls = instantiateUser("Not@mail.com", "Pw123456!", "displayName","USER");
        try {
            Field field = User.class.getDeclaredField("email");
            field.setAccessible(true);
            field.set(userInvalidEmail, null);

            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userWithNulls, null); // id ist null

            Field displayNameField = User.class.getDeclaredField("displayName");
            displayNameField.setAccessible(true);
            displayNameField.set(userWithNulls, null); // displayName ist null

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        userWithNulls.setVerified(true);
        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(userWithNulls)));
        assertThrows(ValidationException.class, () -> userService.getAllUsers(0, 10));


        // User mit zu kurzem username
        var userShortName = instantiateUser("Not@mail.com", "Pw123456!", "displayName","USER");
        try {
            Field displayNameField = User.class.getDeclaredField("displayName");
            displayNameField.setAccessible(true);
            displayNameField.set(userWithNulls, "al"); // displayName ist null
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        userShortName.setVerified(true);
        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(userShortName)));
        assertThrows(ValidationException.class, () -> userService.getAllUsers(0, 10));

        // User mit zu langem username
        String longName = "a".repeat(256);
        User userLongName = instantiateUser("test@example.com", "Pw123456!", "validName","USER");
        try {
            Field displayNameField = User.class.getDeclaredField("displayName");
            displayNameField.setAccessible(true);
            displayNameField.set(userWithNulls, longName); // displayName ist null
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        userLongName.setVerified(true);
        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(userLongName)));
        assertThrows(ValidationException.class, () -> userService.getAllUsers(0, 10));


        //teste leeres Ergebnis
        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        Page<OutUserDto> emptyResult = userService.getAllUsers(0, 10);
        assertEquals((long) 0, emptyResult.getTotalElements());

    }

    @Test
    void updateUser() {
        String validId = "507f1f77bcf86cd799439011";
        var inUserDto = new InUserDto("ValidName");
        var existingUser = instantiateUser("test@example.com", "Pw123456!", "OldName","USER");
        existingUser.setId(validId);
        existingUser.setVerified(true);

        // 1. Normalfall: User wird aktualisiert
        when(repository.findById(validId)).thenReturn(Optional.of(existingUser));
        assertDoesNotThrow(() -> userService.updateUser(validId, inUserDto));
        verify(repository, times(1)).save(any(User.class));

        // ungültige input
        assertThrows(IllegalArgumentException.class,() -> userService.updateUser("1", inUserDto));
        assertThrows(IllegalArgumentException.class,() -> userService.updateUser(validId, null));


        // 3. User nicht gefunden
        when(repository.findById(validId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(validId, inUserDto));
        verify(repository, times(1)).save(any(User.class)); // kein weiterer Aufruf

        // 4. Ungültiger User (ungültige E-Mail)
        var invalidEmailDto = new InUserDto("ValidName");

        var invalidUser = instantiateUser("Not@mail.com", "Pw123456!", "ValidName","USER");
        Field field = null;
        try {
            field = User.class.getDeclaredField("email");
            field.setAccessible(true);
            field.set(invalidUser, "not.valid.email");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        invalidUser.setId(validId);
        invalidUser.setVerified(true);
        when(repository.findById(validId)).thenReturn(Optional.of(invalidUser));
        assertThrows(ValidationException.class, () -> userService.updateUser(validId, invalidEmailDto));
        verify(repository, times(1)).save(any(User.class)); // kein weiterer Aufruf
    }

    @Test
    void deleteUser() {
        // 1. Normalfall: deleteById wird aufgerufen
        String validId = "507f1f77bcf86cd799439011";
        assertDoesNotThrow(() -> userService.deleteUser(validId));
        verify(repository, times(1)).deleteById(validId);

        // 2. Null oder leer: IllegalArgumentException wird geworfen
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(null));
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(""));
    }

    @Test
    void resetPassword() {
        // 1. Normalfall: Passwort wird aktualisiert
        var user = instantiateUser("test@example.com", "OldPassword1!", "TestUser","USER");
        user.setVerified(true);
        var oldPasswordHash = user.getPassword(); // Speichere altes Passwort

        var loginUserDto = new LoginUserDto("test@example.com", "NewPassword1!");
        when(repository.findById("1")).thenReturn(Optional.of(user));
        assertDoesNotThrow(() -> userService.resetPassword("1",loginUserDto));
        assertNotEquals("NewPassword1!", user.getPassword());
        assertNotEquals(user.getPassword(), oldPasswordHash);
        verify(repository, times(1)).save(user);

        // 3. User nicht gefunden
        var notFoundDto = new LoginUserDto("notfound@example.com", "NewPassword1!");
        when(repository.findById("1")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.resetPassword("1",notFoundDto));
        verify(repository, times(1)).save(user); // kein weiterer Aufruf
    }
}