package com.goldbyte.citieye_server.notification.internal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "web_push_subscriptions", schema = "notification")
public class WebPushSubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 30)
    @NotNull
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

    @NotNull
    @Column(name = "endpoint", nullable = false, length = Integer.MAX_VALUE)
    private String endpoint;

    @Size(max = 255)
    @NotNull
    @Column(name = "p256dh", nullable = false)
    private String p256dh;

    @Size(max = 255)
    @NotNull
    @Column(name = "auth", nullable = false)
    private String auth;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
