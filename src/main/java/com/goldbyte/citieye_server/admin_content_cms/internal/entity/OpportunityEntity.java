package com.goldbyte.citieye_server.admin_content_cms.internal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "opportunities", schema = "admins")
public class OpportunityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 100)
    @NotNull
    @Column(name = "opportunity_type", nullable = false)
    private String opportunityType;

    @Size(max = 50)
    @NotNull
    @Column(name = "status", nullable = false)
    private String status;

    @Size(max = 500)
    @NotNull
    @Column(name = "brief", nullable = false)
    private String brief;

    @NotNull
    @Column(name = "full_description", nullable = false, length = Integer.MAX_VALUE)
    private String fullDescription;

    @Convert(converter = StringListConverter.class)
    @Column(name = "required_documents", length = 1000)
    private List<String> requiredDocuments;

    @Size(max = 100)
    @NotNull
    @Column(name = "action_label", nullable = false)
    private String actionLabel;

    @Column(name = "target_cohort_id")
    private Integer targetCohortId;

    @Size(max = 100)
    @Column(name = "target_state_residence")
    private String targetStateResidence;

    @Size(max = 100)
    @Column(name = "target_state_origin")
    private String targetStateOrigin;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
