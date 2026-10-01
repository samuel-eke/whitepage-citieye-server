package com.goldbyte.citieye_server.education.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Entity representing an annual academic update submitted by an enrolled student
 * (primary, secondary, or tertiary level).
 */
@Getter
@Setter
@Entity
@Table(name = "student_academic_updates", schema = "education")
public class StudentAcademicUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 30)
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

    @NotNull
    @Size(max = 50)
    @Column(name = "academic_level", nullable = false, length = 50)
    private String academicLevel; // PRIMARY, SECONDARY, TERTIARY

    @NotNull
    @Size(max = 20)
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear; // e.g. "2024/2025"

    @Size(max = 50)
    @Column(name = "class_grade", length = 50)
    private String classGrade; // e.g. "Primary 6", "SSS 2", "300 Level"

    @Size(max = 150)
    @Column(name = "institution_name", length = 150)
    private String institutionName;

    @Size(max = 500)
    @Column(name = "result_card_url", length = 500)
    private String resultCardUrl; // Stored file path or URL for result card image/document

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "subject_grades", columnDefinition = "jsonb")
    private Map<String, String> subjectGrades; // Dynamic subject-grade mappings e.g. {"Math": "A1"}

    @NotNull
    @Size(max = 30)
    @Column(name = "status", nullable = false, length = 30)
    private String status; // SUBMITTED, VERIFIED, ACTIVE, REJECTED

    @NotNull
    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Size(max = 50)
    @Column(name = "verified_by", length = 50)
    private String verifiedBy;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;
}
