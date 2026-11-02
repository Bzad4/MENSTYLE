package com.menstyle.entity;
import com.menstyle.entity.enums.OrderStatus; import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="order_status_history")
public class OrderStatusHistory extends BaseEntity { @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false) private Order order; @Enumerated(EnumType.STRING) @Column(length=20) private OrderStatus oldStatus; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private OrderStatus newStatus; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="changed_by") private User changedBy; private String note; }
