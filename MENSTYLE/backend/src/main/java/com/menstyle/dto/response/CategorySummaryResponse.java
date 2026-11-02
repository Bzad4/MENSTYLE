package com.menstyle.dto.response;
import com.menstyle.entity.Category;
public record CategorySummaryResponse(Long id,String name,String slug,Long parentId) {
 public static CategorySummaryResponse from(Category c){return new CategorySummaryResponse(c.getId(),c.getName(),c.getSlug(),c.getParent()==null?null:c.getParent().getId());}
}
