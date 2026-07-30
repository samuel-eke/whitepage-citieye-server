package com.goldbyte.citieye_server.citizen_profile.internal.service;

import com.goldbyte.citieye_server.citizen_profile.AgeCohort;
import com.goldbyte.citieye_server.userauth.ProgressiveQuestion;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory registry defining the data-collection questions for each
 * {@link AgeCohort} in the CitiEye progressive profiling system.
 * <p>
 * Each entry maps an {@link AgeCohort} to an ordered list of
 * {@link ProgressiveQuestion} objects representing the fields the government
 * wants to collect for citizens in that life stage.
 * </p>
 * <p>
 * The {@code ProgressiveProfileEventListener} queries this registry at login
 * time, then filters the list to only return questions where the citizen's
 * corresponding profile column is still {@code NULL} in the database.
 * </p>
 */
@Component
public class ProgressiveQuestionRegistry {

    private final Map<AgeCohort, List<ProgressiveQuestion>> registry = new EnumMap<>(AgeCohort.class);
    private final List<ProgressiveQuestion> commonQuestions = new java.util.ArrayList<>();

    @PostConstruct
    public void init() {
        initializeCommonQuestions();

        // =====================================================================
        // Cohort 1: 0–5 (Early Childhood Development)
        // Respondent: Parent / Guardian / Community Health Worker
        // =====================================================================
        registry.put(AgeCohort.EARLY_CHILDHOOD, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("birth_weight_kg")
                        .dataType("DECIMAL")
                        .label("What was the child's birth weight in kilograms?")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("delivery_assistant_type")
                        .dataType("SELECT")
                        .label("Who assisted during the child's delivery?")
                        .category("Health")
                        .options(List.of("DOCTOR", "MIDWIFE", "TRADITIONAL_ATTENDANT", "NONE"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("exclusive_breastfeeding_months")
                        .dataType("NUMBER")
                        .label("How many months was the child exclusively breastfed?")
                        .category("Health")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("vaccination_record_completed")
                        .dataType("BOOLEAN")
                        .label("Has the child completed all routine NPHCDA immunizations?")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("missing_vaccines")
                        .dataType("TEXT")
                        .label("If no, list the vaccines the child has not yet received.")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("is_registered_birth")
                        .dataType("BOOLEAN")
                        .label("Does the child have a National Birth Certificate (NPC)?")
                        .category("Identity")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("parent_guardian_nin")
                        .dataType("TEXT")
                        .label("What is the NIN of the child's primary parent or guardian?")
                        .category("Identity")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("preschool_enrollment")
                        .dataType("BOOLEAN")
                        .label("Is the child currently enrolled in a preschool or early learning center?")
                        .category("Education")
                        .piiSensitive(false)
                        .build()
        ));

        // =====================================================================
        // Cohort 2: 6–11 (Foundational Education)
        // Respondent: Parent / Primary School Administrator
        // =====================================================================
        registry.put(AgeCohort.FOUNDATIONAL_EDUCATION, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("school_name")
                        .dataType("TEXT")
                        .label("What is the name of the school the child is enrolled in?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("current_class_grade")
                        .dataType("SELECT")
                        .label("What is the child's current class grade?")
                        .category("Education")
                        .options(List.of("PRIMARY_1", "PRIMARY_2", "PRIMARY_3", "PRIMARY_4", "PRIMARY_5", "PRIMARY_6"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("finished_primary_6")
                        .dataType("BOOLEAN")
                        .label("Has the pupil completed Primary 6?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("has_first_school_leaving_cert")
                        .dataType("BOOLEAN")
                        .label("Has the pupil obtained the First School Leaving Certificate (FSLC)?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("fslc_grade")
                        .dataType("SELECT")
                        .label("What was the pupil's grade on the FSLC?")
                        .category("Education")
                        .options(List.of("DISTINCTION", "CREDIT", "MERIT", "PASS", "FAIL"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("school_meals_recipient")
                        .dataType("BOOLEAN")
                        .label("Does the child receive meals from the National School Feeding Programme?")
                        .category("Welfare")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("literacy_score_percent")
                        .dataType("NUMBER")
                        .label("What is the child's current reading and comprehension test score (0–100)?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("numeracy_score_percent")
                        .dataType("NUMBER")
                        .label("What is the child's current mathematics test score (0–100)?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("average_attendance_rate")
                        .dataType("NUMBER")
                        .label("What percentage of school days did the child attend last term?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("vision_hearing_pass")
                        .dataType("BOOLEAN")
                        .label("Did the child pass the school eye and ear health check?")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("commute_distance_km")
                        .dataType("DECIMAL")
                        .label("What is the estimated distance from home to school in kilometers?")
                        .category("Infrastructure")
                        .piiSensitive(false)
                        .build()
        ));

        // =====================================================================
        // Cohort 3: 12–18 (Secondary Education & Career Prep)
        // Respondent: Self / School Principal
        // =====================================================================
        registry.put(AgeCohort.SECONDARY_EDUCATION, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("sec_enrollment_status")
                        .dataType("SELECT")
                        .label("What is your current secondary school enrollment status?")
                        .category("Education")
                        .options(List.of("JSS1", "JSS2", "JSS3", "SSS1", "SSS2", "SSS3", "OUT_OF_SCHOOL"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("annual_grade_average")
                        .dataType("DECIMAL")
                        .label("What is your overall academic grade average (%) for the current year?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("registered_waec")
                        .dataType("BOOLEAN")
                        .label("Have you registered for the WAEC (WASSCE) exam?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("waec_grades")
                        .dataType("TEXT")
                        .label("What are your grades on the WAEC exam? (e.g., English: B2, Math: C4)")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("registered_neco")
                        .dataType("BOOLEAN")
                        .label("Have you registered for the NECO exam?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("neco_grades")
                        .dataType("TEXT")
                        .label("What are your grades on the NECO exam? (e.g., English: B2, Math: C5)")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("exam_subsidy_qualified")
                        .dataType("BOOLEAN")
                        .label("Do you qualify for the WAEC/NECO exam fee government waiver?")
                        .category("Welfare")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("preferred_career_track")
                        .dataType("SELECT")
                        .label("What is your intended career pathway?")
                        .category("Career")
                        .options(List.of("STEM", "HUMANITIES", "VOCATIONAL", "COMMERCIAL", "ARTS"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("digital_literacy_cert")
                        .dataType("BOOLEAN")
                        .label("Have you completed a certified basic computer literacy course?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("menstrual_kit_requested")
                        .dataType("BOOLEAN")
                        .label("Do you require free menstrual hygiene kits from the government? (Females only)")
                        .category("Health")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("school_fee_indigent_status")
                        .dataType("BOOLEAN")
                        .label("Are you registered as an indigent student (unable to afford school fees)?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("safeguarding_issue_reported")
                        .dataType("TEXT")
                        .label("Have you experienced or witnessed bullying, harassment, or any form of abuse in school? (Describe briefly or write 'None')")
                        .category("Safeguarding")
                        .piiSensitive(true)
                        .build()
        ));

        // =====================================================================
        // Cohort 4: 19–26 (Higher Education & Skill Acquisition)
        // Respondent: Self / Institution Registrar
        // =====================================================================
        registry.put(AgeCohort.HIGHER_EDUCATION, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("post_sec_pathway")
                        .dataType("SELECT")
                        .label("What is your current post-secondary education pathway?")
                        .category("Education")
                        .options(List.of("UNIVERSITY", "POLYTECHNIC", "VOCATIONAL_COLLEGE", "APPRENTICESHIP", "NOT_IN_EDUCATION"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("tertiary_institution_name")
                        .dataType("TEXT")
                        .label("What is the name of your tertiary institution?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("tertiary_institution_code")
                        .dataType("TEXT")
                        .label("What is your matriculation or registration number at your institution?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nelfund_student_loan")
                        .dataType("BOOLEAN")
                        .label("Are you currently receiving funding from the National Education Loan Fund (NELFUND)?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("cgpa")
                        .dataType("DECIMAL")
                        .label("What is your current Cumulative Grade Point Average (CGPA)?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("expected_graduation_year")
                        .dataType("NUMBER")
                        .label("What year are you expected to graduate?")
                        .category("Education")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nysc_callup_number")
                        .dataType("TEXT")
                        .label("What is your NYSC call-up number?")
                        .category("Career")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nysc_posting_state")
                        .dataType("TEXT")
                        .label("Which state are you currently deployed to for NYSC?")
                        .category("Career")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nysc_ppa_address")
                        .dataType("TEXT")
                        .label("What is the name and address of your NYSC Place of Primary Assignment (PPA)?")
                        .category("Career")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("vocational_licensing")
                        .dataType("TEXT")
                        .label("Do you hold any state-certified vocational or trade license? (e.g., Electrician, Plumber, Nurse). If yes, state the license name.")
                        .category("Career")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("extracurricular_activities")
                        .dataType("TEXT")
                        .label("What extracurricular activities or leadership roles are you involved in? (e.g., Student Union, Sports, Debate, Volunteering)")
                        .category("Social")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("exploitation_abuse_reported")
                        .dataType("TEXT")
                        .label("Have you experienced or witnessed exploitation, academic extortion, physical/verbal abuse, or sexual harassment? (Describe briefly or write 'None')")
                        .category("Safeguarding")
                        .piiSensitive(true)
                        .build()
        ));

        // =====================================================================
        // Cohort 5: 27–32 (Workforce Integration)
        // Respondent: Self
        // =====================================================================
        registry.put(AgeCohort.WORKFORCE_INTEGRATION, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("employment_status")
                        .dataType("SELECT")
                        .label("What is your current employment status?")
                        .category("Career")
                        .options(List.of("EMPLOYED", "SELF_EMPLOYED", "UNDEREMPLOYED", "UNEMPLOYED"))
                        .piiSensitive(false)
                        .build(),

                ProgressiveQuestion.builder()
                        .fieldName("tin_number")
                        .dataType("TEXT")
                        .label("What is your Federal Inland Revenue Service (FIRS) Tax Identification Number (TIN)?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("bvn_number")
                        .dataType("TEXT")
                        .label("What is your Bank Verification Number (BVN)?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("monthly_income_range")
                        .dataType("SELECT")
                        .label("What is your approximate monthly income range?")
                        .category("Financial")
                        .options(List.of("BELOW_30K", "30K_TO_100K", "100K_TO_250K", "250K_TO_500K", "ABOVE_500K", "NO_INCOME"))
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nhis_enrolled")
                        .dataType("BOOLEAN")
                        .label("Are you currently enrolled in the National Health Insurance Scheme (NHIS)?")
                        .category("Health")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("industry_sector")
                        .dataType("SELECT")
                        .label("Which industry sector do you work in?")
                        .category("Career")
                        .options(List.of("TECH", "AGRICULTURE", "HEALTHCARE", "EDUCATION", "PUBLIC_SERVICE", "FINANCE", "RETAIL", "CONSTRUCTION", "MANUFACTURING", "CREATIVE", "OTHER"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("business_registration_number")
                        .dataType("TEXT")
                        .label("If self-employed, what is your CAC (Corporate Affairs Commission) business registration number?")
                        .category("Financial")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nysc_discharge_cert")
                        .dataType("TEXT")
                        .label("What is your NYSC Discharge or Exemption Certificate number?")
                        .category("Career")
                        .piiSensitive(false)
                        .build()
        ));

        // =====================================================================
        // Cohort 6: 33–41 (Career Growth & Family Stability)
        // Respondent: Self
        // =====================================================================
        registry.put(AgeCohort.CAREER_GROWTH, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("marital_status")
                        .dataType("SELECT")
                        .label("What is your current marital status?")
                        .category("Family")
                        .options(List.of("SINGLE", "MARRIED", "DIVORCED", "WIDOWED", "SEPARATED"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("dependent_children_count")
                        .dataType("NUMBER")
                        .label("How many biological or adopted dependent children do you have?")
                        .category("Family")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("housing_tenure_status")
                        .dataType("SELECT")
                        .label("What best describes your current living situation?")
                        .category("Housing")
                        .options(List.of("RENTING", "OWNER_WITH_MORTGAGE", "OWNER_FREE_AND_CLEAR", "SQUATTING", "FAMILY_HOME", "GOVERNMENT_HOUSING"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nhf_contributor")
                        .dataType("BOOLEAN")
                        .label("Are you actively contributing to the National Housing Fund (NHF)?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("cps_pension_pin")
                        .dataType("TEXT")
                        .label("What is your PenCom Contributory Pension Scheme (RSA) PIN?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("farming_land_ownership")
                        .dataType("BOOLEAN")
                        .label("Do you own or lease agricultural land?")
                        .category("Agriculture")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("cooperative_membership")
                        .dataType("TEXT")
                        .label("Are you a member of any registered trade or agrarian cooperative? If yes, state its name.")
                        .category("Financial")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("chronic_health_registry")
                        .dataType("TEXT")
                        .label("Do you have any chronic health conditions? (e.g., Hypertension, Diabetes, Sickle Cell). List them or write 'None'.")
                        .category("Health")
                        .piiSensitive(true)
                        .build()
        ));

        // =====================================================================
        // Cohort 7: 42–44 (Mid-Life Reinvention)
        // Respondent: Self
        // =====================================================================
        registry.put(AgeCohort.MID_LIFE_REINVENTION, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("reskilling_interest")
                        .dataType("BOOLEAN")
                        .label("Are you interested in transitioning to a new career or upskilling in a new field?")
                        .category("Career")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("physical_obsolescence_risk")
                        .dataType("SELECT")
                        .label("How physically demanding is your current job, and how at risk are you of not being able to continue it?")
                        .category("Career")
                        .options(List.of("LOW_RISK", "MEDIUM_RISK", "HIGH_RISK"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("family_education_burden")
                        .dataType("NUMBER")
                        .label("How many of your dependents are currently enrolled in tertiary institutions?")
                        .category("Family")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("business_expansion_capital")
                        .dataType("BOOLEAN")
                        .label("Does your business currently require capital for expansion?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("cardio_screening_date")
                        .dataType("DATE")
                        .label("When did you last have a cardiovascular health screening?")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("primary_income_source")
                        .dataType("SELECT")
                        .label("What is your primary source of income?")
                        .category("Financial")
                        .options(List.of("SALARY", "BUSINESS", "AGRICULTURE", "RENTAL_INCOME", "PENSION", "DEPENDENT_ON_FAMILY", "OTHER"))
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("digital_upskilling_count")
                        .dataType("NUMBER")
                        .label("How many digital or tech-related courses have you completed in the last 2 years?")
                        .category("Career")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("interest_in_political_office")
                        .dataType("BOOLEAN")
                        .label("Are you interested in vying for or running for any political or public office?")
                        .category("Civic")
                        .piiSensitive(true)
                        .build()
        ));

        // =====================================================================
        // Cohort 8: 45–48 (Legacy & Stability Planning)
        // Respondent: Self
        // =====================================================================
        registry.put(AgeCohort.LEGACY_PLANNING, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("estate_plan_will_registered")
                        .dataType("BOOLEAN")
                        .label("Do you have a legally registered Will or Trust document?")
                        .category("Legal")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("land_title_number")
                        .dataType("TEXT")
                        .label("What is your Certificate of Occupancy (C of O) or land registration number (if applicable)?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("life_insurance_enrolled")
                        .dataType("BOOLEAN")
                        .label("Do you currently have an active life insurance policy?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("elder_dependents_count")
                        .dataType("NUMBER")
                        .label("How many elderly parents or relatives are financially dependent on you?")
                        .category("Family")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("hypertension_diabetes_flag")
                        .dataType("BOOLEAN")
                        .label("Have you been clinically diagnosed with hypertension or diabetes?")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("secondary_income_sector")
                        .dataType("SELECT")
                        .label("Do you have a secondary source of income? If yes, which sector?")
                        .category("Financial")
                        .options(List.of("NONE", "POULTRY", "RETAIL", "STOCKS_INVESTMENTS", "REAL_ESTATE", "CONSULTANCY", "OTHER"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("cooperative_savings_status")
                        .dataType("BOOLEAN")
                        .label("Do you actively hold savings in a local cooperative or thrift society?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build()
        ));

        // =====================================================================
        // Cohort 9: 49–54 (Pre-Retirement Transition)
        // Respondent: Self
        // =====================================================================
        registry.put(AgeCohort.PRE_RETIREMENT, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("expected_retirement_age")
                        .dataType("NUMBER")
                        .label("At what age do you anticipate retiring?")
                        .category("Career")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("years_of_service")
                        .dataType("NUMBER")
                        .label("How many total years have you been actively working?")
                        .category("Career")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("retirement_livelihood_plan")
                        .dataType("SELECT")
                        .label("What is your primary livelihood plan after retirement?")
                        .category("Career")
                        .options(List.of("CONSULTING", "FARMING", "RETAIL_BUSINESS", "FULL_REST", "VOLUNTEERING", "OTHER"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("pfa_pension_balance_range")
                        .dataType("SELECT")
                        .label("Approximately how much have you saved in your PFA pension account?")
                        .category("Financial")
                        .options(List.of("BELOW_500K", "500K_TO_2M", "2M_TO_10M", "ABOVE_10M", "NO_PENSION"))
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("relocation_post_retirement")
                        .dataType("TEXT")
                        .label("Which State and LGA do you plan to reside in after retirement?")
                        .category("Housing")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("annual_geriatric_checkup")
                        .dataType("DATE")
                        .label("When did you last have a full geriatric/physical health screening?")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("mortgage_outstanding")
                        .dataType("BOOLEAN")
                        .label("Do you currently have outstanding mortgage debts on your primary home?")
                        .category("Financial")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("co_dependent_students")
                        .dataType("NUMBER")
                        .label("How many school-age children are still financially dependent on you?")
                        .category("Family")
                        .piiSensitive(false)
                        .build()
        ));

        // =====================================================================
        // Cohort 10: 55–60 (Active Aging)
        // Respondent: Self
        // =====================================================================
        registry.put(AgeCohort.ACTIVE_AGING, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("active_aging_status")
                        .dataType("SELECT")
                        .label("What best describes your current activity level?")
                        .category("Social")
                        .options(List.of("SEMI_RETIRED", "FULLY_RETIRED_INACTIVE", "VOLUNTEERING", "STILL_WORKING"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("senior_mentor_interest")
                        .dataType("BOOLEAN")
                        .label("Are you willing to volunteer as a mentor in schools or vocational centers?")
                        .category("Social")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("senior_center_membership")
                        .dataType("BOOLEAN")
                        .label("Are you enrolled in a local senior recreation or support center?")
                        .category("Social")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("mobility_level")
                        .dataType("SELECT")
                        .label("What is your current physical mobility level?")
                        .category("Health")
                        .options(List.of("FULLY_MOBILE", "NEEDS_SOME_ASSISTANCE", "WHEELCHAIR_USER"))
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("primary_geriatric_clinic")
                        .dataType("TEXT")
                        .label("What is the name of the primary healthcare facility you currently access?")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nhis_senior_plan")
                        .dataType("BOOLEAN")
                        .label("Are you enrolled in the NHIS Senior Citizen Plan?")
                        .category("Health")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("social_support_network")
                        .dataType("SELECT")
                        .label("What is your primary social support structure?")
                        .category("Social")
                        .options(List.of("FAMILY_CO_RESIDENT", "COMMUNITY_SUPPORT", "RELIGIOUS_COMMUNITY", "ISOLATED_NO_SUPPORT"))
                        .piiSensitive(false)
                        .build()
        ));

        // =====================================================================
        // Cohort 11: 61–150 (Elderly Care & Support)
        // Respondent: Self / Caregiver / Social Worker
        // =====================================================================
        registry.put(AgeCohort.ELDERLY_CARE, List.of(
                ProgressiveQuestion.builder()
                        .fieldName("pension_disbursement_status")
                        .dataType("SELECT")
                        .label("What is the current status of your pension payments?")
                        .category("Financial")
                        .options(List.of("RECEIVING_ON_TIME", "RECEIVING_WITH_DELAYS", "STOPPED_RECEIVING", "NO_PENSION"))
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("primary_caregiver_name")
                        .dataType("TEXT")
                        .label("What is the full name of your primary caregiver?")
                        .category("Family")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("primary_caregiver_phone")
                        .dataType("TEXT")
                        .label("What is your primary caregiver's phone number?")
                        .category("Family")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("elderly_living_arrangement")
                        .dataType("SELECT")
                        .label("What best describes your current living arrangement?")
                        .category("Housing")
                        .options(List.of("LIVING_ALONE", "WITH_FAMILY", "CARE_HOME", "COMMUNITY_SHELTER"))
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("adl_assistance_level")
                        .dataType("SELECT")
                        .label("How much assistance do you need for daily activities (eating, bathing, dressing)?")
                        .category("Health")
                        .options(List.of("NONE", "SOME_ASSISTANCE", "FULL_ASSISTANCE"))
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("chronic_illness_diagnoses")
                        .dataType("TEXT")
                        .label("List any confirmed chronic illnesses you are currently being treated for. (e.g., Arthritis, Dementia, Heart Disease, or write 'None')")
                        .category("Health")
                        .piiSensitive(true)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("clean_water_electricity")
                        .dataType("BOOLEAN")
                        .label("Does your household have reliable access to clean water and electricity?")
                        .category("Infrastructure")
                        .piiSensitive(false)
                        .build(),
                ProgressiveQuestion.builder()
                        .fieldName("nassp_welfare_registered")
                        .dataType("BOOLEAN")
                        .label("Are you registered under the National Social Safety Nets Programme (NASSP)?")
                        .category("Welfare")
                        .piiSensitive(true)
                        .build()
        ));
    }

    private void initializeCommonQuestions() {
        commonQuestions.add(ProgressiveQuestion.builder()
                .fieldName("current_educational_level")
                .dataType("SELECT")
                .label("What is your current educational level?")
                .category("Education")
                .options(List.of("PRE_PRIMARY", "PRIMARY", "SECONDARY", "TERTIARY", "NONE"))
                .piiSensitive(false)
                .build());

        commonQuestions.add(ProgressiveQuestion.builder()
                .fieldName("highest_academic_qualification")
                .dataType("SELECT")
                .label("What is your highest academic qualification?")
                .category("Education")
                .options(List.of("NONE", "FSLC", "SSCE_WAEC", "SSCE_NECO", "OND", "HND", "BSC", "MSC", "PHD", "PROFESSIONAL_CERT"))
                .piiSensitive(false)
                .build());
    }

    /**
     * Returns the full list of defined questions for a given age cohort.
     *
     * @param cohort the resolved {@link AgeCohort}
     * @return an immutable list of {@link ProgressiveQuestion} objects,
     *         or an empty list if no questions are defined for that cohort
     */
    public List<ProgressiveQuestion> getQuestionsForCohort(AgeCohort cohort) {
        List<ProgressiveQuestion> cohortSpecific = registry.getOrDefault(cohort, List.of());
        List<ProgressiveQuestion> combined = new java.util.ArrayList<>(commonQuestions);
        combined.addAll(cohortSpecific);
        return combined;
    }
}
