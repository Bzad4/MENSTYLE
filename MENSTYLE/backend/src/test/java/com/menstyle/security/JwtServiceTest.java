package com.menstyle.security;

import com.menstyle.entity.Role;
import com.menstyle.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "test-secret-with-at-least-32-characters-long";

    @Test
    void generatesTokenWithUserClaimsAndValidatesIt() {
        JwtService service = new JwtService(SECRET, 3_600_000);
        User user = user(7L, "customer@example.com", "ROLE_CUSTOMER");

        String token = service.generateToken(user);

        assertTrue(service.isTokenValid(token, user.getEmail()));
        assertEquals(user.getEmail(), service.extractUsername(token));
    }

    @Test
    void rejectsExpiredToken() {
        JwtService service = new JwtService(SECRET, -1);
        User user = user(7L, "customer@example.com", "ROLE_CUSTOMER");

        assertFalse(service.isTokenValid(service.generateToken(user), user.getEmail()));
    }

    @Test
    void rejectsTokenForDifferentUser() {
        JwtService service = new JwtService(SECRET, 3_600_000);
        User user = user(7L, "customer@example.com", "ROLE_CUSTOMER");

        assertFalse(service.isTokenValid(service.generateToken(user), "other@example.com"));
    }

    private User user(Long id, String email, String roleName) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        Role role = new Role();
        role.setName(roleName);
        user.setRole(role);
        return user;
    }
}
