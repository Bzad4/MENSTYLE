package com.menstyle.entity;
import com.menstyle.entity.enums.InventoryTransactionType; import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="inventory_transactions")
public class InventoryTransaction extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="variant_id",nullable=false) private ProductVariant variant;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private InventoryTransactionType transactionType;
 @Column(nullable=false) private int quantity; @Column(length=50) private String referenceType; private Long referenceId; private String note;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by") private User createdBy;
}
