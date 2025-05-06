package de.th_rosenheim.ro_co.restapi.security;

import de.th_rosenheim.ro_co.restapi.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void getRole() {
        assertEquals("USER", Role.USER.getRole());
        assertEquals("ADMIN", Role.ADMIN.getRole());
    }

    @Test
    void fromString() {
        assertEquals(Role.USER, Role.fromString("USER"));
        assertEquals(Role.ADMIN, Role.fromString("ADMIN"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("user"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("admin"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("INVALID_ROLE"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(" "));
    }
}