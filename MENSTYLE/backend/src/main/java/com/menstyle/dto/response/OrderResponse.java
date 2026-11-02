package com.menstyle.dto.response;

import com.menstyle.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id, String orderCode, String status, BigDecimal subtotal, BigDecimal shippingFee,
        BigDecimal totalAmount, String paymentMethod, String paymentStatus, LocalDateTime createdAt,
        AddressResponse address, List<OrderItemResponse> items
) {
    public record AddressResponse(String receiverName, String receiverPhone, String province,
                                  String district, String ward, String addressLine) {}
    public record OrderItemResponse(String productName, String sku, String size, String color,
                                    BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
}
