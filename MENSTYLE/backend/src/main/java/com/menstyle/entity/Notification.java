package com.menstyle.entity;
import com.menstyle.entity.enums.NotificationType; import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="notifications",indexes=@Index(name="idx_notifications_user",columnList="user_id"))
public class Notification extends BaseEntity { @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user; @Column(nullable=false) private String title; @Column(nullable=false,columnDefinition="nvarchar(max)") private String content; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private NotificationType notificationType; private Long referenceId; @Column(nullable=false) private boolean isRead; }
