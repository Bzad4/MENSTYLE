package com.menstyle.entity;
import com.menstyle.entity.enums.ProductStatus; import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="banners")
public class Banner extends BaseEntity { @Column(nullable=false) private String title; @Column(nullable=false) private String imageUrl; private String targetUrl; private String position; private LocalDateTime startAt; private LocalDateTime endAt; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ProductStatus status; @Column(nullable=false) private int sortOrder; }
