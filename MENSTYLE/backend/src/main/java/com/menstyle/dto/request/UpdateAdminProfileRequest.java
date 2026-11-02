package com.menstyle.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAdminProfileRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 30) String phone) {
}
