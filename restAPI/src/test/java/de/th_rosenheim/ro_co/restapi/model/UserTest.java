package de.th_rosenheim.ro_co.restapi.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.th_rosenheim.ro_co.restapi.model.User.instantiateUser;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {


    @Test
    void testUserCreation() {
        assertThrows( IllegalArgumentException.class,() -> instantiateUser(null, null, "myName", null));
        assertThrows( IllegalArgumentException.class,() -> instantiateUser("Not@mail.com", "invalid", "myName", null));
        assertThrows( IllegalArgumentException.class,() -> instantiateUser("invalid.mail.com", "Pw123456!", "myName", null));
        assertThrows( IllegalArgumentException.class,() -> instantiateUser("invalid.mail.com", "Pw123456!", "myName", ""));
        assertThrows( IllegalArgumentException.class,() -> instantiateUser("invalid.mail.com", "Pw123456!", "myName", "INVALID_ROLE"));
        assertThrows( IllegalArgumentException.class,() -> instantiateUser("valid@mail.com", "Pw123456!", "", null));
        assertThrows( IllegalArgumentException.class,() -> instantiateUser("valid@mail.com", "Pw123456!", null, null));
        assertThrows( IllegalArgumentException.class,() -> instantiateUser("valid@mail.com", "Pw123456!", "Al", null));

        User user = instantiateUser("valid@mail.com", "Pw123456!", "myName", "USER");
        assertNotNull(user);
        assertNotEquals("Pw123456!", user.getPassword()); // Password should be encrypted
        assertEquals("USER", user.getRole());
        assertEquals("valid@mail.com", user.getEmail());

        User user2 = instantiateUser("valid@mail.com", "Pw123456!", "myName",null);
        assertNotNull(user2);
        assertEquals("USER", user2.getRole()); // Default role should be USER

    }

    @Test
    void getSetRole() {
        User user = instantiateUser("not@mail.com", "Pw123456!", "myName",null);
        assertThrows(IllegalArgumentException.class, () -> user.setRole("INVALID_ROLE"));
        assertThrows(IllegalArgumentException.class  , () -> user.setRole((String) null));
        assertThrows(IllegalArgumentException.class  , () -> user.setRole(""));
        assertThrows(IllegalArgumentException.class  , () -> user.setRole((Role) null));
        assertThrows(IllegalArgumentException.class  , () -> user.setRole("User")); // Case sensitivity check


        user.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN.getRole(), user.getRole());
        assertEquals(Role.ADMIN, user.getRoleEnum());


        user.setRole(Role.USER);
        assertEquals(Role.USER, user.getRoleEnum());
        assertEquals(Role.USER.getRole(), user.getRole());

        user.setRole("ADMIN");
        assertEquals(Role.ADMIN, user.getRoleEnum());
        assertEquals(Role.ADMIN.getRole(), user.getRole());

        user.setRole("USER");
        assertEquals(Role.USER, user.getRoleEnum());
        assertEquals(Role.USER.getRole(), user.getRole());

    }

    @Test
    void testGetSetPassword() {
        User user = instantiateUser("not@mail.com", "Pw123456!", "myName", null);
        assertThrows(IllegalArgumentException.class, () -> user.setPassword(null));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword(""));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("short"));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("noNumber!@#"));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("NoSpecialChar123"));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("nouppercase123!"));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("NOLOWERCASE123!"));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("NoSpecialChar123"));
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("PAsswordisWayToLong123!@#1234567")); // Too long

        String oldPasswordHash = user.getPassword();
        user.setPassword("ValidPw1234!");
        assertNotEquals("ValidPw1234!", user.getPassword()); // Password should be encrypted
        assertNotEquals(oldPasswordHash, user.getPassword());
    }

    @Test
    void testGetSetEmail() {
        User user = instantiateUser("valid@mail.com", "Pw123456!", "myName", null);

        // Null und leer
        assertThrows(IllegalArgumentException.class, () -> user.setEmail(null));
        assertThrows(IllegalArgumentException.class, () -> user.setEmail(""));

        // Ungültige E-Mails (Regex)
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalidmail.com"));
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalid@mail"));
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalid@mail..com"));
        assertThrows(IllegalArgumentException.class, () -> user.setEmail(".invalid@mail.com"));
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalid.@mail.com"));
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalid@.mail.com"));
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalid@mail.com."));
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalid@mail..de"));

        // Gültige E-Mail
        String validEmail = "test.user-123@mailDomain.com";
        user.setEmail(validEmail);
        assertEquals(validEmail, user.getEmail());
    }

    @Test
    void testGetSetDisplayName() {
        User user = instantiateUser("valid@mail.com", "Pw123456!", "myName", null);

        // Null und leer
        assertThrows(IllegalArgumentException.class, () -> user.setDisplayName(null));
        assertThrows(IllegalArgumentException.class, () -> user.setDisplayName(""));

        // Zu kurz
        assertThrows(IllegalArgumentException.class, () -> user.setDisplayName("Al"));

        // Zu lang
        String longName = "A".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> user.setDisplayName(longName));

        // Gültiger Name
        String validName = "Max Mustermann";
        user.setDisplayName(validName);
        assertEquals(validName, user.getDisplayName());
    }

    // src/test/java/de/th_rosenheim/ro_co/restapi/model/UserTest.java

    @Test
    void testAddRemoveGetRefreshTokens() {
        User user = instantiateUser("valid@mail.com", "Pw123456!", "myName", "USER");
        RefreshToken token1 = new RefreshToken("token1Id", "token1Hash");
        RefreshToken token2 = new RefreshToken("token2Id","token2Hash");

        // Anfangs leer
        assertTrue(user.getRefreshTokens().isEmpty());

        // Hinzufügen
        user.addRefreshToken(token1);
        user.addRefreshToken(token2);
        assertEquals(2, user.getRefreshTokens().size());
        assertTrue(user.getRefreshTokens().contains(token1));
        assertTrue(user.getRefreshTokens().contains(token2));

        // Entfernen
        user.removeRefreshToken(token1);
        assertEquals(1, user.getRefreshTokens().size());
        assertFalse(user.getRefreshTokens().contains(token1));
        assertTrue(user.getRefreshTokens().contains(token2));

    }


    @Test
    void testGetAuthorities() {
        User user = instantiateUser("valid@mail.com", "Pw123456!", "myName", "USER");
        assertEquals(1, user.getAuthorities().size());
        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER")));

        user.setRole("ADMIN");
        assertEquals(1, user.getAuthorities().size());
        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));

        // Rolle auf null setzen (über Reflection, da setRole null nicht erlaubt)
        try {
            java.lang.reflect.Field roleField = User.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(user, null);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
        assertTrue(user.getAuthorities().isEmpty());
    }
}