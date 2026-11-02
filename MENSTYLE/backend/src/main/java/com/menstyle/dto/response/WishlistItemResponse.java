package com.menstyle.dto.response;

import com.menstyle.entity.Product;
import java.math.BigDecimal;

public record WishlistItemResponse(Long productId, String name, String slug, BigDecimal basePrice,
                                   String primaryImage, boolean inStock, CategorySummaryResponse category,
                                   BrandSummaryResponse brand) {
    public static WishlistItemResponse from(Product product, String image, boolean inStock) {
        return new WishlistItemResponse(product.getId(), product.getName(), product.getSlug(),
                product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice(),
                image, inStock, CategorySummaryResponse.from(product.getCategory()),
                BrandSummaryResponse.from(product.getBrand()));
    }
}
