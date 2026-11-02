package com.menstyle.dto.response;

import com.menstyle.entity.User;

public record UserResponse(Long id, String fullName, String email, String phone, String role, String status) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(), user.getPhone(),
                user.getRole().getName(), user.getStatus().name());
    }
}
