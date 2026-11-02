package com.menstyle.dto.response;
import com.menstyle.entity.*;
public record ProductVariantResponse(Long id,String sku,String size,String color,java.math.BigDecimal price,int stockQuantity,boolean inStock) {
 public static ProductVariantResponse from(ProductVariant v,Inventory i){int q=i==null?0:Math.max(0,i.getQuantity()-i.getReservedQuantity());return new ProductVariantResponse(v.getId(),v.getSku(),v.getSize().getName(),v.getColor().getName(),v.getPrice(),q,q>0);}
}
