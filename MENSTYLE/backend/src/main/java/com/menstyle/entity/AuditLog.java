package com.menstyle.entity;
import com.menstyle.entity.enums.AuditAction; import jakarta.persistence.*; import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="audit_logs")
public class AuditLog extends BaseEntity { @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private User user; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private AuditAction action; @Column(nullable=false,length=100) private String entityType; private Long entityId; @Column(columnDefinition="nvarchar(max)") private String oldData; @Column(columnDefinition="nvarchar(max)") private String newData; @Column(length=45) private String ipAddress; }
