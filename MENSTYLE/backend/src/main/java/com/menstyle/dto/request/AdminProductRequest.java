package com.menstyle.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AdminProductRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 220) String slug,
        @Size(max = 4000) String description,
        @Size(max = 255) String material,
        @NotNull @PositiveOrZero BigDecimal basePrice,
        @PositiveOrZero BigDecimal salePrice,
        @NotNull Long categoryId,
        @NotNull Long brandId,
        @NotNull String status,
        boolean featured) {
}
