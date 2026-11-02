package com.menstyle.dto.request;

import jakarta.validation.constraints.*;
import com.menstyle.entity.enums.PaymentMethod;

public record CheckoutRequest(
        @NotBlank @Size(max = 150) String receiverName,
        @NotBlank @Pattern(regexp = "^[0-9+() .-]{8,30}$") String receiverPhone,
        @NotBlank @Size(max = 100) String province,
        @NotBlank @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String ward,
        @NotBlank @Size(max = 255) String addressLine,
        @Size(max = 1000) String note,
        @NotNull PaymentMethod paymentMethod,
        @NotBlank @Size(max = 100) String idempotencyKey
) {
}
