package com.menstyle.dto.response;

import com.menstyle.entity.enums.UserStatus;
import java.time.LocalDateTime;

public record AdminCustomerResponse(Long id, String fullName, String email, String phone,
        UserStatus status, long orderCount, java.math.BigDecimal totalSpent, LocalDateTime createdAt) {}
