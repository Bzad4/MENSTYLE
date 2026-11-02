package com.menstyle.dto.response;
import com.menstyle.entity.Product;
public record ProductListItemResponse(Long id,String name,String slug,java.math.BigDecimal basePrice,CategorySummaryResponse category,BrandSummaryResponse brand,String primaryImage,boolean inStock) {
 public static ProductListItemResponse from(Product p,String image,boolean stock){return new ProductListItemResponse(p.getId(),p.getName(),p.getSlug(),p.getSalePrice()!=null?p.getSalePrice():p.getBasePrice(),CategorySummaryResponse.from(p.getCategory()),BrandSummaryResponse.from(p.getBrand()),image,stock);}
}
