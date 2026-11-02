package com.menstyle.entity;
import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="inventories", uniqueConstraints=@UniqueConstraint(name="uk_inventory_variant",columnNames="variant_id"))
public class Inventory extends BaseEntity {
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="variant_id",nullable=false) private ProductVariant variant;
 @Column(nullable=false) private int quantity; @Column(nullable=false) private int reservedQuantity; @Column(nullable=false) private int reorderLevel;
}
