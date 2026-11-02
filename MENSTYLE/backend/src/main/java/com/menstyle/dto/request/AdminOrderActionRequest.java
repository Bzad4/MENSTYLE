package com.menstyle.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminOrderActionRequest(@NotBlank @jakarta.validation.constraints.Size(max = 1000) String note) {}
