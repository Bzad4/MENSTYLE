package com.menstyle.dto.response;

import com.menstyle.entity.*;
import java.math.BigDecimal;

public record CartItemResponse(Long id, Long variantId, Long productId, String productName, String productSlug,
                               String imageUrl, String size, String color, String sku, BigDecimal unitPrice,
                               int quantity, int stockQuantity, BigDecimal lineTotal) {
    public static CartItemResponse from(CartItem item, String imageUrl, int stockQuantity) {
        ProductVariant variant = item.getVariant();
        Product product = variant.getProduct();
        BigDecimal price = variant.getPrice() != null ? variant.getPrice() : product.getBasePrice();
        return new CartItemResponse(item.getId(), variant.getId(), product.getId(), product.getName(),
                product.getSlug(), imageUrl, variant.getSize().getName(), variant.getColor().getName(),
                variant.getSku(), price, item.getQuantity(), stockQuantity,
                price.multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
