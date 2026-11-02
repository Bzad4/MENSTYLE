package com.menstyle.entity;
import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="wishlist_items",uniqueConstraints=@UniqueConstraint(name="uk_wishlist_product",columnNames={"wishlist_id","product_id"}))
public class WishlistItem extends BaseEntity { @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="wishlist_id",nullable=false) private Wishlist wishlist; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id",nullable=false) private Product product; }
