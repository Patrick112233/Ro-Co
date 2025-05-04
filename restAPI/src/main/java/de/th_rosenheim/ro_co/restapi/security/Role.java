package de.th_rosenheim.ro_co.restapi.security;

import java.util.Arrays;

public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    private final String title;

    Role(String role) {
        this.title = role;
    }

    public String getRole() {
        return title;
    }

    public static Role fromString(String role) {
        return Arrays.stream(Role.values())
                .filter(r -> r.getRole().equals(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + role));
    }
}