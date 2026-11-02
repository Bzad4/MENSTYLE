package com.menstyle.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderResponse(Long id, String orderCode, String customerName, String customerEmail,
        String status, String paymentMethod, String paymentStatus, BigDecimal totalAmount,
        LocalDateTime createdAt, OrderResponse.AddressResponse address,
        List<OrderResponse.OrderItemResponse> items, List<OrderDetailResponse.StatusHistory> history) {}
