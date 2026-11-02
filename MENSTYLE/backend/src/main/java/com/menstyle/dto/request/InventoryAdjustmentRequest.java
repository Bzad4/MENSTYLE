package com.menstyle.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InventoryAdjustmentRequest(@Min(0) int quantity, @Min(0) int reorderLevel,
                                         @NotBlank @jakarta.validation.constraints.Size(max = 1000) String note) {}
