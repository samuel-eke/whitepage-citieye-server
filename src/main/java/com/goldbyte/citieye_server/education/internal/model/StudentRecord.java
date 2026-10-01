package com.goldbyte.citieye_server.education.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity representing student records logged by educational officers.
 * Includes state of origin, diagnosed health conditions/special needs,
 * attendance records/rate, and family/guardian background.
 */
@Getter
@Setter
@Entity
@Table(name = "student_records", schema = "education")
public class StudentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 30)
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

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

    @Size(max = 100)
    @Column(name = "student_name", length = 100)
    private String studentName;

    @Size(max = 50)
    @Column(name = "state_of_origin", length = 50)
    private String stateOfOrigin;

    @Size(max = 500)
    @Column(name = "health_conditions_special_needs", length = 500)
    private String healthConditionsSpecialNeeds;

    @DecimalMin(value = "0.00", message = "Attendance rate cannot be less than 0%")
    @DecimalMax(value = "100.00", message = "Attendance rate cannot exceed 100%")
    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @Size(max = 500)
    @Column(name = "family_guardian_background", length = 500)
    private String familyGuardianBackground;

    @Size(max = 20)
    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Size(max = 30)
    @Column(name = "class_grade", length = 30)
    private String classGrade;

    @Size(max = 50)
    @Column(name = "officer_code", length = 50)
    private String officerCode;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
