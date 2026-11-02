package com.menstyle.dto.response;
import com.menstyle.entity.Brand;
public record BrandSummaryResponse(Long id,String name,String slug,String logoUrl) {
 public static BrandSummaryResponse from(Brand b){return new BrandSummaryResponse(b.getId(),b.getName(),b.getSlug(),b.getLogoUrl());}
}
