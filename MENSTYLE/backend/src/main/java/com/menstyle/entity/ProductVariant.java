package com.menstyle.entity;

import com.menstyle.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "product_variants", indexes = @Index(name = "idx_variants_sku", columnList = "sku"),
        uniqueConstraints = {@UniqueConstraint(name = "uk_variants_sku", columnNames = "sku"), @UniqueConstraint(name = "uk_variants_combination", columnNames = {"product_id", "size_id", "color_id"})})
public class ProductVariant extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false) private Product product;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "size_id", nullable = false) private Size size;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "color_id", nullable = false) private Color color;
    @Column(nullable = false, length = 80) private String sku;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal price;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProductStatus status = ProductStatus.ACTIVE;
}
