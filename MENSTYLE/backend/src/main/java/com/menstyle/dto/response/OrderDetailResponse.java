package com.menstyle.dto.response;

import com.menstyle.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id, String orderCode, LocalDateTime createdAt, String status,
        String paymentMethod, String paymentStatus, BigDecimal subtotal, BigDecimal discountAmount,
        BigDecimal shippingFee, BigDecimal totalAmount, String cancellationReason,
        OrderResponse.AddressResponse address, List<Item> items, List<StatusHistory> history
) {
    public record Item(String productName, String sku, String size, String color,
                       BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
    public record StatusHistory(String oldStatus, String newStatus, String note, LocalDateTime createdAt) {}
}
