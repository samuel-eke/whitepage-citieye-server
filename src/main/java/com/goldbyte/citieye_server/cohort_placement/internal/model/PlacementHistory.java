package com.goldbyte.citieye_server.cohort_placement.internal.model;

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
@Table(name = "placement_history", schema = "cohort_placement")
public class PlacementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 30)
    @NotNull
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_cohort_id")
    private Cohort previousCohort;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "new_cohort_id", nullable = false)
    private Cohort newCohort;

    @Size(max = 100)
    @NotNull
    @Column(name = "transition_reason", nullable = false, length = 100)
    private String transitionReason;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "transitioned_at", nullable = false)
    private OffsetDateTime transitionedAt;
}
