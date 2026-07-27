-- Migration: V11__add_progressive_profile_fields.sql
-- Add progressive and conditional data-collection fields to the citizen_profile.profiles table.
-- All columns are optional (nullable) and use appropriate PostgreSQL datatypes.

ALTER TABLE citizen_profile.profiles
    -- 1. Age Cohort: 0-5 (Early Childhood Development)
    ADD COLUMN IF NOT EXISTS birth_weight_kg DECIMAL(4,2),
    ADD COLUMN IF NOT EXISTS delivery_assistant_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS exclusive_breastfeeding_months INT,
    ADD COLUMN IF NOT EXISTS vaccination_record_completed BOOLEAN,
    ADD COLUMN IF NOT EXISTS missing_vaccines VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_registered_birth BOOLEAN,
    ADD COLUMN IF NOT EXISTS parent_guardian_nin CHAR(11),
    ADD COLUMN IF NOT EXISTS preschool_enrollment BOOLEAN,

    -- 2. Age Cohort: 6-11 (Foundational Education)
    ADD COLUMN IF NOT EXISTS school_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS current_class_grade VARCHAR(10),
    ADD COLUMN IF NOT EXISTS finished_primary_6 BOOLEAN,
    ADD COLUMN IF NOT EXISTS has_first_school_leaving_cert BOOLEAN,
    ADD COLUMN IF NOT EXISTS fslc_grade VARCHAR(20),
    ADD COLUMN IF NOT EXISTS school_meals_recipient BOOLEAN,
    ADD COLUMN IF NOT EXISTS literacy_score_percent INT,
    ADD COLUMN IF NOT EXISTS numeracy_score_percent INT,
    ADD COLUMN IF NOT EXISTS average_attendance_rate INT,
    ADD COLUMN IF NOT EXISTS vision_hearing_pass BOOLEAN,
    ADD COLUMN IF NOT EXISTS commute_distance_km DECIMAL(3,1),

    -- 3. Age Cohort: 12-18 (Secondary Education & Career Prep)
    ADD COLUMN IF NOT EXISTS sec_enrollment_status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS annual_grade_average DECIMAL(5,2),
    ADD COLUMN IF NOT EXISTS registered_waec BOOLEAN,
    ADD COLUMN IF NOT EXISTS waec_grades VARCHAR(255),
    ADD COLUMN IF NOT EXISTS registered_neco BOOLEAN,
    ADD COLUMN IF NOT EXISTS neco_grades VARCHAR(255),
    ADD COLUMN IF NOT EXISTS exam_subsidy_qualified BOOLEAN,
    ADD COLUMN IF NOT EXISTS preferred_career_track VARCHAR(50),
    ADD COLUMN IF NOT EXISTS digital_literacy_cert BOOLEAN,
    ADD COLUMN IF NOT EXISTS menstrual_kit_requested BOOLEAN,
    ADD COLUMN IF NOT EXISTS school_fee_indigent_status BOOLEAN,
    ADD COLUMN IF NOT EXISTS safeguarding_issue_reported VARCHAR(100),

    -- 4. Age Cohort: 19-26 (Higher Education & Skill Acquisition)
    ADD COLUMN IF NOT EXISTS tertiary_institution_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS tertiary_institution_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS nelfund_student_loan BOOLEAN,
    ADD COLUMN IF NOT EXISTS cgpa DECIMAL(3,2),
    ADD COLUMN IF NOT EXISTS expected_graduation_year INT,
    ADD COLUMN IF NOT EXISTS nysc_callup_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS nysc_posting_state VARCHAR(50),
    ADD COLUMN IF NOT EXISTS nysc_ppa_address VARCHAR(255),
    ADD COLUMN IF NOT EXISTS vocational_licensing VARCHAR(100),
    ADD COLUMN IF NOT EXISTS extracurricular_activities VARCHAR(255),
    ADD COLUMN IF NOT EXISTS exploitation_abuse_reported VARCHAR(100),

    -- 5. Age Cohort: 27-32 (Workforce Integration)
    ADD COLUMN IF NOT EXISTS employment_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS highest_academic_qualification VARCHAR(50),
    ADD COLUMN IF NOT EXISTS tin_number CHAR(10),
    ADD COLUMN IF NOT EXISTS bvn_number CHAR(11),
    ADD COLUMN IF NOT EXISTS monthly_income_range VARCHAR(50),
    ADD COLUMN IF NOT EXISTS nhis_enrolled BOOLEAN,
    ADD COLUMN IF NOT EXISTS industry_sector VARCHAR(50),
    ADD COLUMN IF NOT EXISTS business_registration_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS nysc_discharge_cert VARCHAR(50),

    -- 6. Age Cohort: 33-41 (Career Growth & Family Stability)
    ADD COLUMN IF NOT EXISTS marital_status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS dependent_children_count INT,
    ADD COLUMN IF NOT EXISTS housing_tenure_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS nhf_contributor BOOLEAN,
    ADD COLUMN IF NOT EXISTS cps_pension_pin VARCHAR(30),
    ADD COLUMN IF NOT EXISTS farming_land_ownership BOOLEAN,
    ADD COLUMN IF NOT EXISTS cooperative_membership VARCHAR(100),
    ADD COLUMN IF NOT EXISTS chronic_health_registry VARCHAR(255),

    -- 7. Age Cohort: 42-44 (Mid-Life Reinvention)
    ADD COLUMN IF NOT EXISTS reskilling_interest BOOLEAN,
    ADD COLUMN IF NOT EXISTS physical_obsolescence_risk VARCHAR(20),
    ADD COLUMN IF NOT EXISTS family_education_burden INT,
    ADD COLUMN IF NOT EXISTS business_expansion_capital BOOLEAN,
    ADD COLUMN IF NOT EXISTS cardio_screening_date DATE,
    ADD COLUMN IF NOT EXISTS primary_income_source VARCHAR(50),
    ADD COLUMN IF NOT EXISTS digital_upskilling_count INT,
    ADD COLUMN IF NOT EXISTS interest_in_political_office BOOLEAN,

    -- 8. Age Cohort: 45-48 (Legacy & Stability Planning)
    ADD COLUMN IF NOT EXISTS estate_plan_will_registered BOOLEAN,
    ADD COLUMN IF NOT EXISTS land_title_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS life_insurance_enrolled BOOLEAN,
    ADD COLUMN IF NOT EXISTS elder_dependents_count INT,
    ADD COLUMN IF NOT EXISTS hypertension_diabetes_flag BOOLEAN,
    ADD COLUMN IF NOT EXISTS secondary_income_sector VARCHAR(50),
    ADD COLUMN IF NOT EXISTS cooperative_savings_status BOOLEAN,

    -- 9. Age Cohort: 49-54 (Pre-Retirement Transition)
    ADD COLUMN IF NOT EXISTS expected_retirement_age INT,
    ADD COLUMN IF NOT EXISTS years_of_service INT,
    ADD COLUMN IF NOT EXISTS retirement_livelihood_plan VARCHAR(50),
    ADD COLUMN IF NOT EXISTS pfa_pension_balance_range VARCHAR(50),
    ADD COLUMN IF NOT EXISTS relocation_post_retirement VARCHAR(100),
    ADD COLUMN IF NOT EXISTS annual_geriatric_checkup DATE,
    ADD COLUMN IF NOT EXISTS mortgage_outstanding BOOLEAN,
    ADD COLUMN IF NOT EXISTS co_dependent_students INT,

    -- 10. Age Cohort: 55-60 (Active Aging)
    ADD COLUMN IF NOT EXISTS active_aging_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS senior_mentor_interest BOOLEAN,
    ADD COLUMN IF NOT EXISTS senior_center_membership BOOLEAN,
    ADD COLUMN IF NOT EXISTS mobility_level VARCHAR(30),
    ADD COLUMN IF NOT EXISTS primary_geriatric_clinic VARCHAR(100),
    ADD COLUMN IF NOT EXISTS nhis_senior_plan BOOLEAN,
    ADD COLUMN IF NOT EXISTS social_support_network VARCHAR(30),

    -- 11. Age Cohort: 60-70+ (Elderly Care & Support)
    ADD COLUMN IF NOT EXISTS pension_disbursement_status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS primary_caregiver_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS primary_caregiver_phone VARCHAR(15),
    ADD COLUMN IF NOT EXISTS elderly_living_arrangement VARCHAR(30),
    ADD COLUMN IF NOT EXISTS adl_assistance_level VARCHAR(30),
    ADD COLUMN IF NOT EXISTS chronic_illness_diagnoses VARCHAR(255),
    ADD COLUMN IF NOT EXISTS clean_water_electricity BOOLEAN,
    ADD COLUMN IF NOT EXISTS nassp_welfare_registered BOOLEAN;
