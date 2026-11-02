package com.menstyle.entity;
import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="order_addresses",uniqueConstraints=@UniqueConstraint(name="uk_order_addresses_order",columnNames="order_id"))
public class OrderAddress extends BaseEntity { @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false) private Order order; @Column(nullable=false,length=150) private String receiverName; @Column(nullable=false,length=30) private String receiverPhone; @Column(nullable=false,length=100) private String province; @Column(nullable=false,length=100) private String district; @Column(nullable=false,length=100) private String ward; @Column(nullable=false,length=255) private String addressDetail; }
