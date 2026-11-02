package com.menstyle.entity;

import com.menstyle.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "brands", indexes = @Index(name = "idx_brands_slug", columnList = "slug"),
        uniqueConstraints = {@UniqueConstraint(name = "uk_brands_name", columnNames = "name"), @UniqueConstraint(name = "uk_brands_slug", columnNames = "slug")})
public class Brand extends BaseEntity {
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 150) private String slug;
    private String logoUrl;
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProductStatus status = ProductStatus.ACTIVE;
}
