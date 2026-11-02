package com.menstyle.service;

import com.menstyle.dto.request.RegisterRequest;
import com.menstyle.entity.Role;
import com.menstyle.entity.User;
import com.menstyle.entity.enums.UserStatus;
import com.menstyle.exception.BadRequestException;
import com.menstyle.repository.*;
import com.menstyle.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock CartRepository cartRepository;
    @Mock WishlistRepository wishlistRepository;
    @Mock PasswordResetTokenRepository resetTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, cartRepository, wishlistRepository,
                resetTokenRepository, passwordEncoder, authenticationManager, jwtService, 3_600_000);
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(
                new RegisterRequest("Test User", " EXISTING@example.com ", "password123", "password123", null)));
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    void rejectsMismatchedPasswords() {
        assertThrows(BadRequestException.class, () -> authService.register(
                new RegisterRequest("Test User", "user@example.com", "password123", "different123", null)));
        verifyNoInteractions(userRepository, roleRepository);
    }

    @Test
    void registersCustomerWithHashedPasswordAndDefaultCollections() {
        Role role = new Role();
        role.setName("ROLE_CUSTOMER");
        User saved = new User();
        saved.setId(1L);
        saved.setEmail("user@example.com");
        saved.setFullName("Test User");
        saved.setRole(role);
        saved.setStatus(UserStatus.ACTIVE);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(java.util.Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        authService.register(new RegisterRequest(" Test User ", "USER@example.com", "password123", "password123", null));

        verify(passwordEncoder).encode("password123");
        verify(cartRepository).save(any());
        verify(wishlistRepository).save(any());
    }

    @Test
    void loginUsesAuthenticationManagerAndReturnsToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFullName("Test User");
        user.setRole(role("ROLE_CUSTOMER"));
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt");

        var response = authService.login(new com.menstyle.dto.request.LoginRequest(
                " USER@example.com ", "password123"));

        assertEquals("jwt", response.accessToken());
        verify(authenticationManager).authenticate(any());
    }

    private Role role(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}
