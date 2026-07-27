package com.goldbyte.citieye_server.analytics.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Full profile analytics DTO — one row per citizen from analytics.citizen_summary.
 * Contains all progressive profile fields so admins can see exactly what each citizen filled in.
 * Null values indicate the citizen has not yet provided that information.
 */
public record CitizenProfileAnalyticsDto(

        // ── Identity ─────────────────────────────────────────────────────
        String citizenCode,
        String firstName,
        String middleName,
        String lastName,
        String phoneNumber,
        String email,
        LocalDate dateOfBirth,
        Integer age,
        String gender,
        String stateOfOrigin,
        String stateOfResidence,
        String lga,
        String address,
        OffsetDateTime registeredAt,

        // ── Cohort ────────────────────────────────────────────────────────
        String cohortName,
        Integer ageAtRegistration,
        OffsetDateTime cohortLastTransitionedAt,

        // ── Basic Profile ─────────────────────────────────────────────────
        String hometown,
        String currentEducationalLevel,
        String highestEducationalQualification,
        String courseOfStudy,
        String universityAttended,
        String degreeAttained,
        Integer yearOfGraduation,
        Boolean isExpectantParent,
        Integer householdSize,
        Boolean isPregnant,
        String bloodGroup,
        String bloodGenotype,
        String currentEmploymentStatus,
        String placeOfEmploymentBusiness,
        String nameOfEmployerBusiness,
        String natureOfBusinessJob,

        // ── Early Childhood (0-5) ─────────────────────────────────────────
        BigDecimal birthWeightKg,
        String deliveryAssistantType,
        Integer exclusiveBreastfeedingMonths,
        Boolean vaccinationRecordCompleted,
        String missingVaccines,
        Boolean isRegisteredBirth,
        String parentGuardianNin,
        Boolean preschoolEnrollment,

        // ── Foundational Education (6-11) ─────────────────────────────────
        String schoolName,
        String currentClassGrade,
        Boolean finishedPrimary6,
        Boolean hasFirstSchoolLeavingCert,
        String fslcGrade,
        Boolean schoolMealsRecipient,
        Integer literacyScorePercent,
        Integer numeracyScorePercent,
        Integer averageAttendanceRate,
        Boolean visionHearingPass,
        BigDecimal commuteDistanceKm,

        // ── Secondary Education (12-18) ───────────────────────────────────
        String secEnrollmentStatus,
        BigDecimal annualGradeAverage,
        Boolean registeredWaec,
        String waecGrades,
        Boolean registeredNeco,
        String necoGrades,
        Boolean examSubsidyQualified,
        String preferredCareerTrack,
        Boolean digitalLiteracyCert,
        Boolean menstrualKitRequested,
        Boolean schoolFeeIndigentStatus,
        String safeguardingIssueReported,

        // ── Higher Education (19-26) ──────────────────────────────────────
        String tertiaryInstitutionName,
        String tertiaryInstitutionCode,
        Boolean nelfundStudentLoan,
        BigDecimal cgpa,
        Integer expectedGraduationYear,
        String nyscCallupNumber,
        String nyscPostingState,
        String nyscPpaAddress,
        String vocationalLicensing,
        String extracurricularActivities,
        String exploitationAbuseReported,

        // ── Workforce Integration (27-32) ─────────────────────────────────
        String employmentStatus,
        String highestAcademicQualification,
        String tinNumber,
        String bvnNumber,
        String monthlyIncomeRange,
        Boolean nhisEnrolled,
        String industrySector,
        String businessRegistrationNumber,
        String nyscDischargeCert,

        // ── Career Growth (33-41) ─────────────────────────────────────────
        String maritalStatus,
        Integer dependentChildrenCount,
        String housingTenureStatus,
        Boolean nhfContributor,
        String cpsPensionPin,
        Boolean farmingLandOwnership,
        String cooperativeMembership,
        String chronicHealthRegistry,

        // ── Mid-Life Reinvention (42-44) ──────────────────────────────────
        Boolean reskillingInterest,
        String physicalObsolescenceRisk,
        Integer familyEducationBurden,
        Boolean businessExpansionCapital,
        LocalDate cardioScreeningDate,
        String primaryIncomeSource,
        Integer digitalUpskillingCount,
        Boolean interestInPoliticalOffice,

        // ── Legacy Planning (45-48) ───────────────────────────────────────
        Boolean estatePlanWillRegistered,
        String landTitleNumber,
        Boolean lifeInsuranceEnrolled,
        Integer elderDependentsCount,
        Boolean hypertensionDiabetesFlag,
        String secondaryIncomeSector,
        Boolean cooperativeSavingsStatus,

        // ── Pre-Retirement (49-54) ────────────────────────────────────────
        Integer expectedRetirementAge,
        Integer yearsOfService,
        String retirementLivelihoodPlan,
        String pfaPensionBalanceRange,
        String relocationPostRetirement,
        LocalDate annualGeriatricCheckup,
        Boolean mortgageOutstanding,
        Integer coDependentStudents,

        // ── Active Aging (55-60) ──────────────────────────────────────────
        String activeAgingStatus,
        Boolean seniorMentorInterest,
        Boolean seniorCenterMembership,
        String mobilityLevel,
        String primaryGeriatricClinic,
        Boolean nhisSeniorPlan,
        String socialSupportNetwork,

        // ── Elderly Care (61+) ────────────────────────────────────────────
        String pensionDisbursementStatus,
        String primaryCaregiverName,
        String primaryCaregiverPhone,
        String elderlyLivingArrangement,
        String adlAssistanceLevel,
        String chronicIllnessDiagnoses,
        Boolean cleanWaterElectricity,
        Boolean nassaWelfareRegistered,

        OffsetDateTime profileLastUpdated
) {}
