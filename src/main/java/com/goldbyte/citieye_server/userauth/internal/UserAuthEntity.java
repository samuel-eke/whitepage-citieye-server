package com.goldbyte.citieye_server.userauth.internal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "auth")
public class UserAuthEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number", length = 14)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = Integer.MAX_VALUE)
    private String passwordHash;

    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @ColumnDefault("false")
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @ColumnDefault("false")
    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = Integer.MAX_VALUE)
    private String mfaSecret;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @ColumnDefault("0")
    @Column(name = "failed_login_attempts", nullable = false)
    private Short failedLoginAttempts;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

/*
 TODO [Reverse Engineering] create field to map the 'role' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "role", columnDefinition = "user_role not null")
    private Object role;
*/
}