package com.goldbyte.citieye_server.citizen_profile.internal.model;

import com.goldbyte.citieye_server.citizen_profile.internal.model.enums.EmploymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "profiles", schema = "citizen_profile")
public class Profile {

    @Id
    @Size(max = 30)
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

    @Size(max = 100)
    @Column(name = "lga", length = 100)
    private String lga;

    @Size(max = 100)
    @Column(name = "hometown", length = 100)
    private String hometown;

    @Size(max = 100)
    @Column(name = "current_educational_level", length = 100)
    private String currentEducationalLevel;

    @Size(max = 100)
    @Column(name = "highest_educational_qualification", length = 100)
    private String highestEducationalQualification;

    @Size(max = 100)
    @Column(name = "course_of_study", length = 100)
    private String courseOfStudy;

    @Column(name = "age")
    private Integer age;

    @Size(max = 255)
    @Column(name = "university_attended", length = 255)
    private String universityAttended;

    @Size(max = 100)
    @Column(name = "degree_attained", length = 100)
    private String degreeAttained;

    @Column(name = "year_of_graduation")
    private Integer yearOfGraduation;

    @Column(name = "is_expectant_parent")
    private Boolean isExpectantParent;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "is_pregnant")
    private Boolean isPregnant;

    @Size(max = 5)
    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Size(max = 5)
    @Column(name = "blood_genotype", length = 5)
    private String bloodGenotype;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_employment_status", columnDefinition = "employment_status_type")
    private EmploymentStatus currentEmploymentStatus;

    @Size(max = 255)
    @Column(name = "place_of_employment_business", length = 255)
    private String placeOfEmploymentBusiness;

    @Size(max = 255)
    @Column(name = "name_of_employer_business", length = 255)
    private String nameOfEmployerBusiness;

    @Size(max = 255)
    @Column(name = "nature_of_business_job", length = 255)
    private String natureOfBusinessJob;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // =========================================================================
    // Progressive Profile Fields (V11) — All Optional / Nullable
    // =========================================================================

    // --- Cohort 1: 0–5 (Early Childhood Development) ---
    @Column(name = "birth_weight_kg", precision = 4, scale = 2)
    private java.math.BigDecimal birthWeightKg;

    @Size(max = 50)
    @Column(name = "delivery_assistant_type", length = 50)
    private String deliveryAssistantType;

    @Column(name = "exclusive_breastfeeding_months")
    private Integer exclusiveBreastfeedingMonths;

    @Column(name = "vaccination_record_completed")
    private Boolean vaccinationRecordCompleted;

    @Size(max = 255)
    @Column(name = "missing_vaccines", length = 255)
    private String missingVaccines;

    @Column(name = "is_registered_birth")
    private Boolean isRegisteredBirth;

    @Size(max = 11)
    @Column(name = "parent_guardian_nin", length = 11)
    private String parentGuardianNin;

    @Column(name = "preschool_enrollment")
    private Boolean preschoolEnrollment;

    // --- Cohort 2: 6–11 (Foundational Education) ---
    @Size(max = 150)
    @Column(name = "school_name", length = 150)
    private String schoolName;

    @Size(max = 10)
    @Column(name = "current_class_grade", length = 10)
    private String currentClassGrade;

    @Column(name = "finished_primary_6")
    private Boolean finishedPrimary6;

    @Column(name = "has_first_school_leaving_cert")
    private Boolean hasFirstSchoolLeavingCert;

    @Size(max = 20)
    @Column(name = "fslc_grade", length = 20)
    private String fslcGrade;

    @Column(name = "school_meals_recipient")
    private Boolean schoolMealsRecipient;

    @Column(name = "literacy_score_percent")
    private Integer literacyScorePercent;

    @Column(name = "numeracy_score_percent")
    private Integer numeracyScorePercent;

    @Column(name = "average_attendance_rate")
    private Integer averageAttendanceRate;

    @Column(name = "vision_hearing_pass")
    private Boolean visionHearingPass;

    @Column(name = "commute_distance_km", precision = 3, scale = 1)
    private java.math.BigDecimal commuteDistanceKm;

    // --- Cohort 3: 12–18 (Secondary Education & Career Prep) ---
    @Size(max = 20)
    @Column(name = "sec_enrollment_status", length = 20)
    private String secEnrollmentStatus;

    @Column(name = "annual_grade_average", precision = 5, scale = 2)
    private java.math.BigDecimal annualGradeAverage;

    @Column(name = "registered_waec")
    private Boolean registeredWaec;

    @Size(max = 255)
    @Column(name = "waec_grades", length = 255)
    private String waecGrades;

    @Column(name = "registered_neco")
    private Boolean registeredNeco;

    @Size(max = 255)
    @Column(name = "neco_grades", length = 255)
    private String necoGrades;

    @Column(name = "exam_subsidy_qualified")
    private Boolean examSubsidyQualified;

    @Size(max = 50)
    @Column(name = "preferred_career_track", length = 50)
    private String preferredCareerTrack;

    @Column(name = "digital_literacy_cert")
    private Boolean digitalLiteracyCert;

    @Column(name = "menstrual_kit_requested")
    private Boolean menstrualKitRequested;

    @Column(name = "school_fee_indigent_status")
    private Boolean schoolFeeIndigentStatus;

    @Size(max = 100)
    @Column(name = "safeguarding_issue_reported", length = 100)
    private String safeguardingIssueReported;

    // --- Cohort 4: 19–26 (Higher Education & Skill Acquisition) ---
    @Size(max = 50)
    @Column(name = "post_sec_pathway", length = 50)
    private String postSecPathway;

    @Size(max = 150)
    @Column(name = "tertiary_institution_name", length = 150)
    private String tertiaryInstitutionName;

    @Size(max = 50)
    @Column(name = "tertiary_institution_code", length = 50)
    private String tertiaryInstitutionCode;

    @Column(name = "nelfund_student_loan")
    private Boolean nelfundStudentLoan;

    @Column(name = "cgpa", precision = 3, scale = 2)
    private java.math.BigDecimal cgpa;

    @Column(name = "expected_graduation_year")
    private Integer expectedGraduationYear;

    @Size(max = 50)
    @Column(name = "nysc_callup_number", length = 50)
    private String nyscCallupNumber;

    @Size(max = 50)
    @Column(name = "nysc_posting_state", length = 50)
    private String nyscPostingState;

    @Size(max = 255)
    @Column(name = "nysc_ppa_address", length = 255)
    private String nyscPpaAddress;

    @Size(max = 100)
    @Column(name = "vocational_licensing", length = 100)
    private String vocationalLicensing;

    @Size(max = 255)
    @Column(name = "extracurricular_activities", length = 255)
    private String extracurricularActivities;

    @Size(max = 100)
    @Column(name = "exploitation_abuse_reported", length = 100)
    private String exploitationAbuseReported;

    // --- Cohort 5: 27–32 (Workforce Integration) ---
    @Size(max = 30)
    @Column(name = "employment_status", length = 30)
    private String employmentStatus;

    @Size(max = 50)
    @Column(name = "highest_academic_qualification", length = 50)
    private String highestAcademicQualification;

    @Size(max = 10)
    @Column(name = "tin_number", length = 10)
    private String tinNumber;

    @Size(max = 11)
    @Column(name = "bvn_number", length = 11)
    private String bvnNumber;

    @Size(max = 50)
    @Column(name = "monthly_income_range", length = 50)
    private String monthlyIncomeRange;

    @Column(name = "nhis_enrolled")
    private Boolean nhisEnrolled;

    @Size(max = 50)
    @Column(name = "industry_sector", length = 50)
    private String industrySector;

    @Size(max = 20)
    @Column(name = "business_registration_number", length = 20)
    private String businessRegistrationNumber;

    @Size(max = 50)
    @Column(name = "nysc_discharge_cert", length = 50)
    private String nyscDischargeCert;

    // --- Cohort 6: 33–41 (Career Growth & Family Stability) ---
    @Size(max = 20)
    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "dependent_children_count")
    private Integer dependentChildrenCount;

    @Size(max = 30)
    @Column(name = "housing_tenure_status", length = 30)
    private String housingTenureStatus;

    @Column(name = "nhf_contributor")
    private Boolean nhfContributor;

    @Size(max = 30)
    @Column(name = "cps_pension_pin", length = 30)
    private String cpsPensionPin;

    @Column(name = "farming_land_ownership")
    private Boolean farmingLandOwnership;

    @Size(max = 100)
    @Column(name = "cooperative_membership", length = 100)
    private String cooperativeMembership;

    @Size(max = 255)
    @Column(name = "chronic_health_registry", length = 255)
    private String chronicHealthRegistry;

    // --- Cohort 7: 42–44 (Mid-Life Reinvention) ---
    @Column(name = "reskilling_interest")
    private Boolean reskillingInterest;

    @Size(max = 20)
    @Column(name = "physical_obsolescence_risk", length = 20)
    private String physicalObsolescenceRisk;

    @Column(name = "family_education_burden")
    private Integer familyEducationBurden;

    @Column(name = "business_expansion_capital")
    private Boolean businessExpansionCapital;

    @Column(name = "cardio_screening_date")
    private LocalDate cardioScreeningDate;

    @Size(max = 50)
    @Column(name = "primary_income_source", length = 50)
    private String primaryIncomeSource;

    @Column(name = "digital_upskilling_count")
    private Integer digitalUpskillingCount;

    @Column(name = "interest_in_political_office")
    private Boolean interestInPoliticalOffice;

    // --- Cohort 8: 45–48 (Legacy & Stability Planning) ---
    @Column(name = "estate_plan_will_registered")
    private Boolean estatePlanWillRegistered;

    @Size(max = 50)
    @Column(name = "land_title_number", length = 50)
    private String landTitleNumber;

    @Column(name = "life_insurance_enrolled")
    private Boolean lifeInsuranceEnrolled;

    @Column(name = "elder_dependents_count")
    private Integer elderDependentsCount;

    @Column(name = "hypertension_diabetes_flag")
    private Boolean hypertensionDiabetesFlag;

    @Size(max = 50)
    @Column(name = "secondary_income_sector", length = 50)
    private String secondaryIncomeSector;

    @Column(name = "cooperative_savings_status")
    private Boolean cooperativeSavingsStatus;

    // --- Cohort 9: 49–54 (Pre-Retirement Transition) ---
    @Column(name = "expected_retirement_age")
    private Integer expectedRetirementAge;

    @Column(name = "years_of_service")
    private Integer yearsOfService;

    @Size(max = 50)
    @Column(name = "retirement_livelihood_plan", length = 50)
    private String retirementLivelihoodPlan;

    @Size(max = 50)
    @Column(name = "pfa_pension_balance_range", length = 50)
    private String pfaPensionBalanceRange;

    @Size(max = 100)
    @Column(name = "relocation_post_retirement", length = 100)
    private String relocationPostRetirement;

    @Column(name = "annual_geriatric_checkup")
    private LocalDate annualGeriatricCheckup;

    @Column(name = "mortgage_outstanding")
    private Boolean mortgageOutstanding;

    @Column(name = "co_dependent_students")
    private Integer coDependentStudents;

    // --- Cohort 10: 55–60 (Active Aging) ---
    @Size(max = 30)
    @Column(name = "active_aging_status", length = 30)
    private String activeAgingStatus;

    @Column(name = "senior_mentor_interest")
    private Boolean seniorMentorInterest;

    @Column(name = "senior_center_membership")
    private Boolean seniorCenterMembership;

    @Size(max = 30)
    @Column(name = "mobility_level", length = 30)
    private String mobilityLevel;

    @Size(max = 100)
    @Column(name = "primary_geriatric_clinic", length = 100)
    private String primaryGeriatricClinic;

    @Column(name = "nhis_senior_plan")
    private Boolean nhisSeniorPlan;

    @Size(max = 30)
    @Column(name = "social_support_network", length = 30)
    private String socialSupportNetwork;

    // --- Cohort 11: 60–70+ (Elderly Care & Support) ---
    @Size(max = 20)
    @Column(name = "pension_disbursement_status", length = 20)
    private String pensionDisbursementStatus;

    @Size(max = 100)
    @Column(name = "primary_caregiver_name", length = 100)
    private String primaryCaregiverName;

    @Size(max = 15)
    @Column(name = "primary_caregiver_phone", length = 15)
    private String primaryCaregiverPhone;

    @Size(max = 30)
    @Column(name = "elderly_living_arrangement", length = 30)
    private String elderlyLivingArrangement;

    @Size(max = 30)
    @Column(name = "adl_assistance_level", length = 30)
    private String adlAssistanceLevel;

    @Size(max = 255)
    @Column(name = "chronic_illness_diagnoses", length = 255)
    private String chronicIllnessDiagnoses;

    @Column(name = "clean_water_electricity")
    private Boolean cleanWaterElectricity;

    @Column(name = "nassp_welfare_registered")
    private Boolean nassWelfareRegistered;
}
