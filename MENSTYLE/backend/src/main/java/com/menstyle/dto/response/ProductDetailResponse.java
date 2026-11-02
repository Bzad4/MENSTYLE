package com.menstyle.dto.response;
import com.menstyle.entity.Product; import java.util.List;
public record ProductDetailResponse(Long id,String name,String slug,String description,java.math.BigDecimal basePrice,CategorySummaryResponse category,BrandSummaryResponse brand,List<String> images,List<ProductVariantResponse> variants,String status) {
 public static ProductDetailResponse from(Product p,List<String> images,List<ProductVariantResponse> variants){return new ProductDetailResponse(p.getId(),p.getName(),p.getSlug(),p.getDescription(),p.getSalePrice()!=null?p.getSalePrice():p.getBasePrice(),CategorySummaryResponse.from(p.getCategory()),BrandSummaryResponse.from(p.getBrand()),images,variants,p.getStatus().name());}
}
