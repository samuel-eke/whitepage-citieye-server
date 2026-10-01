package com.goldbyte.citieye_server.education.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entity binding an educational officer to a specific School and LGA jurisdiction
 * for strict role-based data isolation.
 */
@Getter
@Setter
@Entity
@Table(name = "officer_school_assignments", schema = "education")
public class OfficerSchoolAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "officer_code", nullable = false, length = 50)
    private String officerCode;

    @NotNull
    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Size(max = 150)
    @Column(name = "school_name", length = 150)
    private String schoolName;

    @NotNull
    @Size(max = 100)
    @Column(name = "lga", nullable = false, length = 100)
    private String lga;

    @Size(max = 50)
    @Column(name = "state", length = 50)
    private String state;

    @NotNull
    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
