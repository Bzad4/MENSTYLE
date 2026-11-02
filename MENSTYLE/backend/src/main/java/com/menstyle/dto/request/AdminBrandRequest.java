package com.menstyle.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminBrandRequest(@NotBlank @Size(max = 120) String name,
                                @NotBlank @Size(max = 150) String slug,
                                String logoUrl, @Size(max = 1000) String description,
                                @NotBlank String status) {
}
