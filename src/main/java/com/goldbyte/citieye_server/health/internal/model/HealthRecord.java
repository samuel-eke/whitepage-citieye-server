package com.goldbyte.citieye_server.health.internal.model;

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
@Table(name = "health_records", schema = "health")
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 30)
    @NotNull
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

    @Size(max = 30)
    @NotNull
    @Column(name = "officer_code", nullable = false, length = 30)
    private String officerCode;

    @Size(max = 50)
    @NotNull
    @Column(name = "record_type", nullable = false, length = 50)
    private String recordType;

    @NotNull
    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "logged_at", nullable = false)
    private OffsetDateTime loggedAt;
}
