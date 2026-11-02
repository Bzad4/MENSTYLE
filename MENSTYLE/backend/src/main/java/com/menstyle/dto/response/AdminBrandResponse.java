package com.menstyle.dto.response;

import com.menstyle.entity.Brand;

public record AdminBrandResponse(Long id, String name, String slug, String logoUrl,
                                 String description, String status) {
    public static AdminBrandResponse from(Brand b) {
        return new AdminBrandResponse(b.getId(), b.getName(), b.getSlug(), b.getLogoUrl(),
                b.getDescription(), b.getStatus().name());
    }
}
