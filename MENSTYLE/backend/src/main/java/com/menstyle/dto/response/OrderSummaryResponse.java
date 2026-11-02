package com.menstyle.dto.response;

import com.menstyle.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id, String orderCode, LocalDateTime createdAt, String orderStatus,
        String paymentMethod, String paymentStatus, BigDecimal totalAmount, int itemCount
) {
    public static OrderSummaryResponse from(Order order, int itemCount) {
        return new OrderSummaryResponse(order.getId(), order.getOrderCode(), order.getCreatedAt(),
                order.getOrderStatus().name(), order.getPaymentMethod().name(),
                order.getPaymentStatus().name(), order.getTotalAmount(), itemCount);
    }
}
