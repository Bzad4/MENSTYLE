package com.menstyle.dto.response;

import com.menstyle.entity.enums.UserStatus;
import java.time.LocalDateTime;

public record AdminUserResponse(Long id, String fullName, String email, String role,
        UserStatus status, LocalDateTime createdAt) {}
