package com.menstyle.dto.response;

import com.menstyle.entity.Category;

public record AdminCategoryResponse(Long id, String name, String slug, String description,
                                    String imageUrl, Long parentId, String status) {
    public static AdminCategoryResponse from(Category c) {
        return new AdminCategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(),
                c.getImageUrl(), c.getParent() == null ? null : c.getParent().getId(), c.getStatus().name());
    }
}
