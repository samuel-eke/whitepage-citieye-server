-- Migration: V22__add_residence_lga.sql
-- 1. Add residence_lga column to users_auth and admin_user
ALTER TABLE auth.users_auth ADD COLUMN IF NOT EXISTS residence_lga VARCHAR(100);
ALTER TABLE auth.admin_user ADD COLUMN IF NOT EXISTS residence_lga VARCHAR(100);

-- Populate existing rows to satisfy the constraints
UPDATE auth.users_auth SET residence_lga = COALESCE(lga, 'Unknown') WHERE residence_lga IS NULL AND role = 'citizen';
UPDATE auth.admin_user SET residence_lga = COALESCE(lga, 'Unknown') WHERE residence_lga IS NULL AND role IN ('field_officer', 'field_officer_health', 'field_officer_education');

-- 2. Add validation check constraints for residence_lga
ALTER TABLE auth.users_auth ADD CONSTRAINT chk_citizen_residence_lga CHECK (role != 'citizen' OR residence_lga IS NOT NULL);
ALTER TABLE auth.admin_user ADD CONSTRAINT chk_officer_residence_lga CHECK (role NOT IN ('field_officer', 'field_officer_health', 'field_officer_education') OR residence_lga IS NOT NULL);

-- 3. Update compute_user_citizen_code trigger function to use a random 6-digit suffix for citizen and field officers
CREATE OR REPLACE FUNCTION auth.compute_user_citizen_code()
RETURNS TRIGGER AS $$
DECLARE
    state_abbr TEXT;
    lga_prefix TEXT;
    random_suffix TEXT;
BEGIN
    CASE NEW.role
        -- CITIZEN format: CTI-[STATE(abbrv)]-[LGA(first three letters)]-[6-DIGIT-RANDOM]
        WHEN 'citizen' THEN
            state_abbr := auth.get_state_abbreviation(NEW.state_of_origin);
            lga_prefix := UPPER(SUBSTRING(REGEXP_REPLACE(NEW.lga, '[^a-zA-Z]', '', 'g'), 1, 3));
            IF LENGTH(lga_prefix) < 3 THEN
                lga_prefix := RPAD(lga_prefix, 3, 'X');
            END IF;
            random_suffix := auth.generate_random_suffix(6, TRUE);
            NEW.citizen_code := 'CTI-' || state_abbr || '-' || lga_prefix || '-' || random_suffix;

        -- FIELD OFFICER format: CIT-OFR-[STATE OF ORIGIN]-[LGA(first three letters)]-[6-DIGIT-RANDOM]
        WHEN 'field_officer', 'field_officer_health', 'field_officer_education' THEN
            state_abbr := auth.get_state_abbreviation(NEW.state_of_origin);
            lga_prefix := UPPER(SUBSTRING(REGEXP_REPLACE(NEW.lga, '[^a-zA-Z]', '', 'g'), 1, 3));
            IF LENGTH(lga_prefix) < 3 THEN
                lga_prefix := RPAD(lga_prefix, 3, 'X');
            END IF;
            random_suffix := auth.generate_random_suffix(6, TRUE);
            NEW.citizen_code := 'CIT-OFR-' || state_abbr || '-' || lga_prefix || '-' || random_suffix;

        -- AGENCY ADMIN format: cit-adm-[6 numeric digits]
        WHEN 'agency_admin' THEN
            NEW.citizen_code := 'cit-adm-' || auth.generate_random_suffix(6, TRUE);

        -- SUPER ADMIN format: cit-super-[6 numeric digits]
        WHEN 'super_admin' THEN
            NEW.citizen_code := 'cit-super-' || auth.generate_random_suffix(6, TRUE);
    END CASE;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. Recreate citizen_summary materialized view to include residence_lga
DROP MATERIALIZED VIEW IF EXISTS analytics.citizen_summary CASCADE;

CREATE MATERIALIZED VIEW analytics.citizen_summary AS
SELECT
    u.citizen_code,
    u.first_name,
    u.middle_name,
    u.last_name,
    u.phone_number,
    u.email,
    u.date_of_birth,
    u.state_of_residence::text   AS state_of_residence,
    u.state_of_origin::text      AS state_of_origin,
    u.lga,
    u.residence_lga,
    u.gender::text               AS gender,
    u.nin,
    u.parent_nin,
    u.address,
    u.created_at                 AS registered_at,

    -- Calculated age
    date_part('year', age(u.date_of_birth))::int AS age,

    -- Cohort assignment
    c.name                       AS cohort_name,
    cp.age_at_registration,
    cp.last_transitioned_at      AS cohort_last_transitioned_at,

    -- Basic profile fields
    p.hometown,
    p.current_educational_level,
    p.highest_educational_qualification,
    p.course_of_study,
    p.university_attended,
    p.degree_attained,
    p.year_of_graduation,
    p.is_expectant_parent,
    p.household_size,
    p.is_pregnant,
    p.blood_group,
    p.blood_genotype,
    p.current_employment_status::text AS current_employment_status,
    p.place_of_employment_business,
    p.nature_of_business_job,
    p.name_of_employer_business,
    p.updated_at                 AS profile_last_updated,

    -- Progressive profile details (Early Childhood)
    p.birth_weight_kg,
    p.delivery_assistant_type,
    p.exclusive_breastfeeding_months,
    p.vaccination_record_completed,
    p.missing_vaccines,
    p.is_registered_birth,
    p.parent_guardian_nin,
    p.preschool_enrollment,

    -- Foundational Education
    p.school_name,
    p.current_class_grade,
    p.finished_primary_6,
    p.has_first_school_leaving_cert,
    p.fslc_grade,
    p.school_meals_recipient,
    p.literacy_score_percent,
    p.numeracy_score_percent,
    p.average_attendance_rate,
    p.vision_hearing_pass,
    p.commute_distance_km,

    -- Secondary Education
    p.sec_enrollment_status,
    p.annual_grade_average,
    p.registered_waec,
    p.waec_grades,
    p.registered_neco,
    p.neco_grades,
    p.exam_subsidy_qualified,
    p.preferred_career_track,
    p.digital_literacy_cert,
    p.menstrual_kit_requested,
    p.school_fee_indigent_status,
    p.safeguarding_issue_reported,

    -- Tertiary/Higher Ed
    p.post_sec_pathway,
    p.tertiary_institution_name,
    p.tertiary_institution_code,
    p.nelfund_student_loan,
    p.cgpa,
    p.expected_graduation_year,
    p.nysc_callup_number,
    p.nysc_posting_state,
    p.nysc_ppa_address,
    p.vocational_licensing,
    p.extracurricular_activities,
    p.exploitation_abuse_reported,

    -- Workforce
    p.employment_status,
    p.highest_academic_qualification,
    p.tin_number,
    p.bvn_number,
    p.monthly_income_range,
    p.nhis_enrolled,
    p.industry_sector,
    p.business_registration_number,
    p.nysc_discharge_cert,

    -- Career Growth
    p.marital_status,
    p.dependent_children_count,
    p.housing_tenure_status,
    p.nhf_contributor,
    p.cps_pension_pin,
    p.farming_land_ownership,
    p.cooperative_membership,
    p.chronic_health_registry,

    -- Mid-life
    p.reskilling_interest,
    p.physical_obsolescence_risk,
    p.family_education_burden,
    p.business_expansion_capital,
    p.cardio_screening_date,
    p.primary_income_source,
    p.digital_upskilling_count,
    p.interest_in_political_office,

    -- Legacy
    p.estate_plan_will_registered,
    p.land_title_number,
    p.life_insurance_enrolled,
    p.elder_dependents_count,
    p.hypertension_diabetes_flag,
    p.secondary_income_sector,
    p.cooperative_savings_status,

    -- Pre-retirement
    p.expected_retirement_age,
    p.years_of_service,
    p.retirement_livelihood_plan,
    p.pfa_pension_balance_range,
    p.relocation_post_retirement,
    p.annual_geriatric_checkup,
    p.mortgage_outstanding,
    p.co_dependent_students,

    -- Active aging
    p.active_aging_status,
    p.senior_mentor_interest,
    p.senior_center_membership,
    p.mobility_level,
    p.primary_geriatric_clinic,
    p.nhis_senior_plan,
    p.social_support_network,

    -- Elderly Care
    p.pension_disbursement_status,
    p.primary_caregiver_name,
    p.primary_caregiver_phone,
    p.elderly_living_arrangement,
    p.adl_assistance_level,
    p.chronic_illness_diagnoses,
    p.clean_water_electricity,
    p.nassp_welfare_registered
FROM auth.users_auth u
LEFT JOIN citizen_profile.profiles p ON u.citizen_code = p.citizen_code
LEFT JOIN cohort_placement.placements cp ON u.citizen_code = cp.citizen_code
LEFT JOIN cohort_placement.cohorts c ON cp.cohort_id = c.id
WHERE u.role = 'citizen'
WITH DATA;

-- 5. Recreate indexes for analytics.citizen_summary
CREATE UNIQUE INDEX idx_mv_citizen_summary_code ON analytics.citizen_summary (citizen_code);
CREATE INDEX idx_mv_citizen_summary_state ON analytics.citizen_summary (state_of_origin);
CREATE INDEX idx_mv_citizen_summary_lga ON analytics.citizen_summary (lga);
CREATE INDEX idx_mv_citizen_summary_residence_lga ON analytics.citizen_summary (residence_lga);
CREATE INDEX idx_mv_citizen_summary_cohort ON analytics.citizen_summary (cohort_name);
CREATE INDEX idx_mv_citizen_summary_gender ON analytics.citizen_summary (gender);
CREATE INDEX idx_mv_citizen_summary_registered ON analytics.citizen_summary (registered_at);
