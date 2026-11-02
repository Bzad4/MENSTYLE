package com.menstyle.entity;
import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="cart_items",uniqueConstraints=@UniqueConstraint(name="uk_cart_items_variant",columnNames={"cart_id","variant_id"}))
public class CartItem extends BaseEntity { @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="cart_id",nullable=false) private Cart cart; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="variant_id",nullable=false) private ProductVariant variant; @Column(nullable=false) private int quantity; }
