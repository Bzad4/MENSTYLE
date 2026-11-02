package com.menstyle.dto.response;

import com.menstyle.entity.*;
import java.math.BigDecimal;

public record AdminVariantResponse(Long id, Long sizeId, String size, Long colorId, String color,
                                   String sku, BigDecimal price, String status,
                                   int quantity, int reservedQuantity) {
    public static AdminVariantResponse from(ProductVariant v, Inventory i) {
        return new AdminVariantResponse(v.getId(), v.getSize().getId(), v.getSize().getName(),
                v.getColor().getId(), v.getColor().getName(), v.getSku(), v.getPrice(),
                v.getStatus().name(), i == null ? 0 : i.getQuantity(), i == null ? 0 : i.getReservedQuantity());
    }
}
