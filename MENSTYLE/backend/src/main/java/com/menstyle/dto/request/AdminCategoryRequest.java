package com.menstyle.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCategoryRequest(@NotBlank @Size(max = 150) String name,
                                   @NotBlank @Size(max = 180) String slug,
                                   @Size(max = 1000) String description,
                                   String imageUrl, Long parentId, @NotBlank String status) {
}
