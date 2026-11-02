package com.menstyle.service;

import com.menstyle.dto.request.ChangePasswordRequest;
import com.menstyle.dto.request.UpdateAdminProfileRequest;
import com.menstyle.dto.response.UserResponse;
import com.menstyle.entity.User;
import com.menstyle.exception.BadRequestException;
import com.menstyle.exception.ResourceNotFoundException;
import com.menstyle.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccountService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Authentication authentication) {
        return UserResponse.from(findUser(authentication));
    }

    @Transactional
    public UserResponse updateProfile(Authentication authentication, UpdateAdminProfileRequest request) {
        User user = findUser(authentication);
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone() == null ? null : request.phone().trim());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("New passwords do not match");
        }
        User user = findUser(authentication);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User findUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("Admin account was not found");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Admin account was not found"));
    }
}
