package com.goldbyte.citieye_server.health.internal.model;

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
@Table(name = "health_records", schema = "health")
public class HealthRecord {

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

    @NotNull
    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "logged_at", nullable = false)
    private OffsetDateTime loggedAt;

    // --- Early Childhood Fields (Ages 0-5) ---
    @Column(name = "early_has_received_vaccines")
    private Boolean earlyHasReceivedVaccines;

    @Column(name = "early_bcg_vaccination_completed")
    private Boolean earlyBcgVaccinationCompleted;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_polio_vaccination_series", columnDefinition = "jsonb")
    private Map<String, Object> earlyPolioVaccinationSeries;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_dpt_pentavalent_series", columnDefinition = "jsonb")
    private Map<String, Object> earlyDptPentavalentSeries;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_measles_mmr_vaccination", columnDefinition = "jsonb")
    private Map<String, Object> earlyMeaslesMmrVaccination;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_yellow_fever_vaccination", columnDefinition = "jsonb")
    private Map<String, Object> earlyYellowFeverVaccination;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_all_vaccines_uptodate", columnDefinition = "jsonb")
    private Map<String, Object> earlyAllVaccinesUptodate;

    @Column(name = "early_vaccination_card_available")
    private Boolean earlyVaccinationCardAvailable;

    @Column(name = "early_exclusively_breastfed_6months")
    private Boolean earlyExclusivelyBreastfed6months;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_currently_breastfeeding", columnDefinition = "jsonb")
    private Map<String, Object> earlyCurrentlyBreastfeeding;

    @Column(name = "early_receiving_supplementary_foods")
    private Boolean earlyReceivingSupplementaryFoods;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_birth_weight", columnDefinition = "jsonb")
    private Map<String, Object> earlyBirthWeight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_current_weight_height", columnDefinition = "jsonb")
    private Map<String, Object> earlyCurrentWeightHeight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_severe_malnutrition_signs", columnDefinition = "jsonb")
    private Map<String, Object> earlySevereMalnutritionSigns;

    @Column(name = "early_receives_supplementary_nutrition_program")
    private Boolean earlyReceivesSupplementaryNutritionProgram;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_ever_hospitalized", columnDefinition = "jsonb")
    private Map<String, Object> earlyEverHospitalized;

    @Column(name = "early_current_health_condition", length = 50)
    private String earlyCurrentHealthCondition;

    @Column(name = "early_malaria_treatment_3months")
    private Boolean earlyMalariaTreatment3months;

    @Column(name = "early_diarrheal_disease_treatment_3months")
    private Boolean earlyDiarrhealDiseaseTreatment3months;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_congenital_chronic_condition", columnDefinition = "jsonb")
    private Map<String, Object> earlyCongenitalChronicCondition;

    @Column(name = "early_mother_age_at_birth")
    private Integer earlyMotherAgeAtBirth;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_mother_received_anc", columnDefinition = "jsonb")
    private Map<String, Object> earlyMotherReceivedAnc;

    @Column(name = "early_delivery_setting", length = 50)
    private String earlyDeliverySetting;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_mother_received_pnc", columnDefinition = "jsonb")
    private Map<String, Object> earlyMotherReceivedPnc;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_developmental_milestones_met", columnDefinition = "jsonb")
    private Map<String, Object> earlyDevelopmentalMilestonesMet;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "early_developmental_concerns", columnDefinition = "jsonb")
    private Map<String, Object> earlyDevelopmentalConcerns;

    // --- School Age Fields (Ages 6-11) ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_dpt_td_booster", columnDefinition = "jsonb")
    private Map<String, Object> schoolDptTdBooster;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_measles_yf_booster", columnDefinition = "jsonb")
    private Map<String, Object> schoolMeaslesYfBooster;

    @Column(name = "school_vaccination_card_available")
    private Boolean schoolVaccinationCardAvailable;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_current_height_weight", columnDefinition = "jsonb")
    private Map<String, Object> schoolCurrentHeightWeight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_malnutrition_signs", columnDefinition = "jsonb")
    private Map<String, Object> schoolMalnutritionSigns;

    @Column(name = "school_receives_school_meals")
    private Boolean schoolReceivesSchoolMeals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_dietary_restrictions_allergies", columnDefinition = "jsonb")
    private Map<String, Object> schoolDietaryRestrictionsAllergies;

    @Column(name = "school_current_symptoms", length = 50)
    private String schoolCurrentSymptoms;

    @Column(name = "school_parasitic_infection_treatment")
    private Boolean schoolParasiticInfectionTreatment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_malaria_dengue_past_year", columnDefinition = "jsonb")
    private Map<String, Object> schoolMalariaDenguePastYear;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_permanent_health_condition", columnDefinition = "jsonb")
    private Map<String, Object> schoolPermanentHealthCondition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_illness_absences_past_term", columnDefinition = "jsonb")
    private Map<String, Object> schoolIllnessAbsencesPastTerm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "school_feels_safe_healthy_at_school", columnDefinition = "jsonb")
    private Map<String, Object> schoolFeelsSafeHealthyAtSchool;

    @Column(name = "school_water_sanitation_at_school")
    private Boolean schoolWaterSanitationAtSchool;

    @Column(name = "school_can_see_clearly")
    private Boolean schoolCanSeeClearly;

    @Column(name = "school_can_hear_clearly")
    private Boolean schoolCanHearClearly;

    @Column(name = "school_health_awareness", length = 50)
    private String schoolHealthAwareness;

    @Column(name = "school_knows_when_to_seek_health_services")
    private Boolean schoolKnowsWhenToSeekHealthServices;

    // --- Adolescent Fields (Ages 12-18) ---
    @Column(name = "adolescent_hpv_cervical_cancer_vaccine")
    private Boolean adolescentHpvCervicalCancerVaccine;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_menstruation_started", columnDefinition = "jsonb")
    private Map<String, Object> adolescentMenstruationStarted;

    @Column(name = "adolescent_menstrual_cycle_regular")
    private Boolean adolescentMenstrualCycleRegular;

    @Column(name = "adolescent_received_sex_education")
    private Boolean adolescentReceivedSexEducation;

    @Column(name = "adolescent_aware_contraception_safe_sex")
    private Boolean adolescentAwareContraceptionSafeSex;

    @Column(name = "adolescent_sexually_active")
    private Boolean adolescentSexuallyActive;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_contraception_use", columnDefinition = "jsonb")
    private Map<String, Object> adolescentContraceptionUse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_ever_pregnant_fathered", columnDefinition = "jsonb")
    private Map<String, Object> adolescentEverPregnantFathered;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_sti_history", columnDefinition = "jsonb")
    private Map<String, Object> adolescentStiHistory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_depression_anxiety_worry", columnDefinition = "jsonb")
    private Map<String, Object> adolescentDepressionAnxietyWorry;

    @Column(name = "adolescent_self_harm_suicidal_thoughts")
    private Boolean adolescentSelfHarmSuicidalThoughts;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_trauma_abuse_experience", columnDefinition = "jsonb")
    private Map<String, Object> adolescentTraumaAbuseExperience;

    @Column(name = "adolescent_access_mental_health_support")
    private Boolean adolescentAccessMentalHealthSupport;

    @Column(name = "adolescent_feels_supported")
    private Boolean adolescentFeelsSupported;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_tobacco_cigarette_use", columnDefinition = "jsonb")
    private Map<String, Object> adolescentTobaccoCigaretteUse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_alcohol_use", columnDefinition = "jsonb")
    private Map<String, Object> adolescentAlcoholUse;

    @Column(name = "adolescent_drug_use")
    private Boolean adolescentDrugUse;

    @Column(name = "adolescent_screen_time_internet_usage", precision = 5, scale = 2)
    private BigDecimal adolescentScreenTimeInternetUsage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_current_height_weight", columnDefinition = "jsonb")
    private Map<String, Object> adolescentCurrentHeightWeight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_family_history_chronic_disease", columnDefinition = "jsonb")
    private Map<String, Object> adolescentFamilyHistoryChronicDisease;

    @Column(name = "adolescent_physical_activity_hours", precision = 5, scale = 2)
    private BigDecimal adolescentPhysicalActivityHours;

    @Column(name = "adolescent_dietary_habits", length = 50)
    private String adolescentDietaryHabits;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_chronic_health_condition", columnDefinition = "jsonb")
    private Map<String, Object> adolescentChronicHealthCondition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adolescent_disability", columnDefinition = "jsonb")
    private Map<String, Object> adolescentDisability;

    @Column(name = "adolescent_disability_support_at_school")
    private Boolean adolescentDisabilitySupportAtSchool;

    // --- Common / All-Ages Fields ---
    @Size(max = 5)
    @Column(name = "common_blood_group", length = 5)
    private String commonBloodGroup;

    @Size(max = 5)
    @Column(name = "common_blood_genotype", length = 5)
    private String commonBloodGenotype;

    @Column(name = "common_is_expectant_parent")
    private Boolean commonIsExpectantParent;

    @Column(name = "common_is_pregnant")
    private Boolean commonIsPregnant;

    @Column(name = "common_history_of_diabetes")
    private Boolean commonHistoryOfDiabetes;

    @Size(max = 255)
    @Column(name = "common_hereditary_disease", length = 255)
    private String commonHereditaryDisease;

    @Column(name = "common_nhis_enrolled")
    private Boolean commonNhisEnrolled;

    @Column(name = "common_nhis_senior_plan_active")
    private Boolean commonNhisSeniorPlanActive;
}
