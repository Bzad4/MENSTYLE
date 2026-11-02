package com.menstyle.dto.request;

import jakarta.validation.constraints.*;

public record AdminUserRequest(@NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email String email, @Size(max = 30) String phone,
        @NotBlank @Pattern(regexp = "ADMIN|STAFF") String role,
        @NotBlank @Size(min = 8, max = 100) String password) {}
