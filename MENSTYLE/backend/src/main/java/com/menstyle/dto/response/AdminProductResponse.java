package com.menstyle.dto.response;

import com.menstyle.entity.Product;
import java.math.BigDecimal;
import java.util.List;

public record AdminProductResponse(Long id, String name, String slug, String description,
        String material, BigDecimal basePrice, BigDecimal salePrice, String status, boolean featured,
        Long categoryId, String categoryName, Long brandId, String brandName,
        List<String> images, List<AdminVariantResponse> variants) {
    public static AdminProductResponse from(Product p, List<String> images, List<AdminVariantResponse> variants) {
        return new AdminProductResponse(p.getId(), p.getName(), p.getSlug(), p.getDescription(),
                p.getMaterial(), p.getBasePrice(), p.getSalePrice(), p.getStatus().name(), p.isFeatured(),
                p.getCategory().getId(), p.getCategory().getName(), p.getBrand().getId(), p.getBrand().getName(),
                images, variants);
    }
}
