package com.menstyle.entity;
import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="carts",uniqueConstraints=@UniqueConstraint(name="uk_carts_user",columnNames="user_id"))
public class Cart extends BaseEntity { @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user; }
