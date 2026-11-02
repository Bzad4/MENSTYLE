package com.menstyle.service;

import com.menstyle.dto.request.*;
import com.menstyle.dto.response.AuthResponse;
import com.menstyle.dto.response.UserResponse;
import com.menstyle.entity.*;
import com.menstyle.entity.enums.UserStatus;
import com.menstyle.exception.BadRequestException;
import com.menstyle.repository.*;
import com.menstyle.security.JwtService;
import com.menstyle.util.TokenHashing;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private static final String CUSTOMER_ROLE = "ROLE_CUSTOMER";
    private static final long RESET_TOKEN_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long jwtExpirationMillis;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       CartRepository cartRepository, WishlistRepository wishlistRepository,
                       PasswordResetTokenRepository resetTokenRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       @Value("${security.jwt.expiration-ms:3600000}") long jwtExpirationMillis) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cartRepository = cartRepository;
        this.wishlistRepository = wishlistRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtExpirationMillis = jwtExpirationMillis;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }
        Role role = roleRepository.findByName(CUSTOMER_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default customer role is missing"));
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        User saved = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(saved);
        cartRepository.save(cart);
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(saved);
        wishlistRepository.save(wishlist);
        return UserResponse.from(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        user.setLastLoginAt(LocalDateTime.now());
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtExpirationMillis / 1000, UserResponse.from(user));
    }

    @Transactional
    public void createResetToken(String email) {
        userRepository.findByEmail(normalizeEmail(email)).ifPresent(user -> {
            resetTokenRepository.findByUserIdAndUsedAtIsNull(user.getId()).forEach(token -> {
                token.setUsedAt(LocalDateTime.now());
            });
            String rawToken = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(TokenHashing.sha256(rawToken));
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
            resetTokenRepository.save(resetToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        PasswordResetToken token = resetTokenRepository.findByTokenHash(TokenHashing.sha256(request.token()))
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        token.setUsedAt(LocalDateTime.now());
        userRepository.save(user);
        resetTokenRepository.save(token);
    }

    @Transactional
    public UserResponse currentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user was not found"));
        return UserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
