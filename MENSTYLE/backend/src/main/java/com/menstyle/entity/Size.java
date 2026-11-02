package com.menstyle.entity;

import com.menstyle.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "sizes", uniqueConstraints = @UniqueConstraint(name = "uk_sizes_name", columnNames = "name"))
public class Size {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 20) private String name;
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProductStatus status = ProductStatus.ACTIVE;
}
