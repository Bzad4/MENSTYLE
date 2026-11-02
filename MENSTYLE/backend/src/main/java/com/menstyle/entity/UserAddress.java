package com.menstyle.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "user_addresses")
public class UserAddress extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, length = 150) private String receiverName;
    @Column(nullable = false, length = 30) private String receiverPhone;
    @Column(nullable = false, length = 100) private String province;
    @Column(nullable = false, length = 100) private String district;
    @Column(nullable = false, length = 100) private String ward;
    @Column(nullable = false, length = 255) private String addressDetail;
    @Column(nullable = false) private boolean isDefault;
}
