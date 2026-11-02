package com.menstyle.entity;

import com.menstyle.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "users", indexes = @Index(name = "idx_users_email", columnList = "email"),
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "role_id", nullable = false) private Role role;
    @Column(nullable = false, length = 150) private String fullName;
    @Column(nullable = false, length = 254) private String email;
    @Column(length = 30) private String phone;
    @Column(nullable = false) private String passwordHash;
    private String avatarUrl;
    @Column(length = 20) private String gender;
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserStatus status = UserStatus.ACTIVE;
    @Column(nullable = false) private boolean emailVerified;
    private LocalDateTime lastLoginAt;
}
