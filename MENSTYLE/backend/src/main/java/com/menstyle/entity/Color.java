package com.menstyle.entity;

import com.menstyle.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "colors")
public class Color {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 50) private String name;
    @Column(length = 20) private String colorCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProductStatus status = ProductStatus.ACTIVE;
}
