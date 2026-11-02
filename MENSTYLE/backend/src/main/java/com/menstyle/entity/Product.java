package com.menstyle.entity;

import com.menstyle.entity.enums.ProductStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "products", indexes = {
        @Index(name = "idx_products_slug", columnList = "slug"), @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_brand", columnList = "brand_id")}, uniqueConstraints = @UniqueConstraint(name = "uk_products_slug", columnNames = "slug"))
public class Product extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id", nullable = false) private Category category;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "brand_id", nullable = false) private Brand brand;
    @Nationalized
    @Column(nullable = false, length = 200) private String name;
    @Column(nullable = false, length = 220) private String slug;
    @Column(columnDefinition = "nvarchar(max)") private String description;
    private String material;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal basePrice;
    @Column(precision = 19, scale = 2) private BigDecimal salePrice;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProductStatus status = ProductStatus.ACTIVE;
    @Column(nullable = false) private boolean featured;
}
