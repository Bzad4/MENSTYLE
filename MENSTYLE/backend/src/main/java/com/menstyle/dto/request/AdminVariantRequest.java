package com.menstyle.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AdminVariantRequest(@NotNull Long sizeId, @NotNull Long colorId,
                                  @NotBlank @Size(max = 80) String sku,
                                  @NotNull @PositiveOrZero BigDecimal price,
                                  @NotBlank String status, @PositiveOrZero Integer initialStock) {
}
