package com.goldbyte.citieye_server.education.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entity maintaining an audit-ready, real-time log of academic status transitions
 * to preserve continuous academic progression history.
 */
@Getter
@Setter
@Entity
@Table(name = "academic_status_transitions", schema = "education")
public class AcademicStatusTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "update_id")
    private Long updateId; // Optional reference to student_academic_updates id

    @NotNull
    @Size(max = 30)
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

    @NotNull
    @Size(max = 50)
    @Column(name = "academic_level", nullable = false, length = 50)
    private String academicLevel;

    @NotNull
    @Size(max = 20)
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Size(max = 30)
    @Column(name = "previous_status", length = 30)
    private String previousStatus;

    @NotNull
    @Size(max = 30)
    @Column(name = "status", nullable = false, length = 30)
    private String status; // SUBMITTED, VERIFIED, ACTIVE, REJECTED

    @NotNull
    @Column(name = "transition_timestamp", nullable = false)
    private OffsetDateTime transitionTimestamp;

    @Size(max = 50)
    @Column(name = "performed_by", length = 50)
    private String performedBy; // Citizen code or officer code

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;
}
