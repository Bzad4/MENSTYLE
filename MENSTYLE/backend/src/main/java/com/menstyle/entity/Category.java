package com.menstyle.entity;

import com.menstyle.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "categories", indexes = @Index(name = "idx_categories_slug", columnList = "slug"),
        uniqueConstraints = @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug"))
public class Category extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id") private Category parent;
    @Column(nullable = false, length = 150) private String name;
    @Column(nullable = false, length = 180) private String slug;
    private String description;
    private String imageUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProductStatus status = ProductStatus.ACTIVE;
}
