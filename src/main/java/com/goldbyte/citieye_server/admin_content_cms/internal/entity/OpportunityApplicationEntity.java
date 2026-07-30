package com.goldbyte.citieye_server.admin_content_cms.internal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "opportunity_applications", schema = "admins")
public class OpportunityApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 30)
    @NotNull
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityEntity opportunity;

    @Size(max = 50)
    @NotNull
    @Column(name = "application_status", nullable = false, length = 50)
    private String applicationStatus;

    @Size(max = 30)
    @Column(name = "assigned_officer_code", length = 30)
    private String assignedOfficerCode;

    @Column(name = "officer_notes", length = Integer.MAX_VALUE)
    private String officerNotes;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
