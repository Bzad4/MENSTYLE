package com.menstyle.entity;
import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="wishlists",uniqueConstraints=@UniqueConstraint(name="uk_wishlists_user",columnNames="user_id"))
public class Wishlist extends BaseEntity { @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user; }
