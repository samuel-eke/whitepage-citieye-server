package com.goldbyte.citieye_server.education.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "education_records", schema = "education")
public class EducationRecord {

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

    @Size(max = 150)
    @NotNull
    @Column(name = "institution_name", nullable = false, length = 150)
    private String institutionName;

    @Size(max = 15)
    @NotNull
    @Column(name = "academic_year", nullable = false, length = 15)
    private String academicYear;

    @Size(max = 20)
    @Column(name = "class_grade", length = 20)
    private String classGrade;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "subject_grades", columnDefinition = "jsonb")
    private Map<String, String> subjectGrades;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "logged_at", nullable = false)
    private OffsetDateTime loggedAt;

    // --- Primary School Fields (Ages 6-11) ---
    @Column(name = "primary_class_grade", length = 20)
    private String primaryClassGrade;

    @Column(name = "primary_attendance_rate_tier", length = 15)
    private String primaryAttendanceRateTier;

    @Column(name = "primary_english_literacy_pass")
    private Boolean primaryEnglishLiteracyPass;

    @Column(name = "primary_basic_numeracy_pass")
    private Boolean primaryBasicNumeracyPass;

    @Column(name = "primary_materials_access")
    private Boolean primaryMaterialsAccess;

    @Column(name = "primary_class_repeating")
    private Boolean primaryClassRepeating;

    @Column(name = "primary_ever_dropped_out")
    private Boolean primaryEverDroppedOut;

    @Column(name = "primary_school_feeding_recipient")
    private Boolean primarySchoolFeedingRecipient;

    @Column(name = "primary_absent_due_to_hunger")
    private Boolean primaryAbsentDueToHunger;

    @Column(name = "primary_commute_distance", length = 50)
    private String primaryCommuteDistance;

    @Column(name = "primary_guardian_education", length = 50)
    private String primaryGuardianEducation;

    @Column(name = "primary_guardian_homework_support", length = 15)
    private String primaryGuardianHomeworkSupport;

    @Column(name = "primary_sibling_in_school_count")
    private Integer primarySiblingInSchoolCount;

    @Column(name = "primary_subject_strengths", length = 200)
    private String primarySubjectStrengths;

    @Column(name = "primary_vocational_interest", length = 50)
    private String primaryVocationalInterest;

    // --- Secondary School Fields (Ages 12-18) ---
    @Column(name = "sec_school_type", length = 50)
    private String secSchoolType;

    @Column(name = "sec_enrolled")
    private Boolean secEnrolled;

    @Column(name = "sec_attendance_rate_tier", length = 15)
    private String secAttendanceRateTier;

    @Column(name = "sec_suspension_history")
    private Boolean secSuspensionHistory;

    @Column(name = "sec_class_repeating")
    private Boolean secClassRepeating;

    @Column(name = "sec_exams_registered", length = 100)
    private String secExamsRegistered;

    @Column(name = "sec_exam_tutoring_access", length = 50)
    private String secExamTutoringAccess;

    @Column(name = "sec_post_secondary_plan", length = 50)
    private String secPostSecondaryPlan;

    @Column(name = "sec_career_interests", length = 200)
    private String secCareerInterests;

    @Column(name = "sec_guardian_employment", length = 50)
    private String secGuardianEmployment;

    @Column(name = "sec_fees_paid_in_full")
    private Boolean secFeesPaidInFull;

    @Column(name = "sec_working_while_in_school")
    private Boolean secWorkingWhileInSchool;

    @Column(name = "sec_daily_chores_burden", length = 15)
    private String secDailyChoresBurden;

    @Column(name = "sec_attendance_barriers", length = 200)
    private String secAttendanceBarriers;

    @Column(name = "sec_counselor_access")
    private Boolean secCounselorAccess;

    @Column(name = "sec_dropout_pressure_reasons", length = 200)
    private String secDropoutPressureReasons;

    @Column(name = "sec_extracurricular_activities")
    private Boolean secExtracurricularActivities;

    @Column(name = "sec_digital_literacy_check")
    private Boolean secDigitalLiteracyCheck;

    @Column(name = "sec_spoken_languages", length = 200)
    private String secSpokenLanguages;
}
