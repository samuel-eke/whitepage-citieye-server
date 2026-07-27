-- Migration: V17__analytics_materialized_views.sql
-- Creates the analytics schema with materialized views for fast dashboard queries.
-- Joins across auth, citizen_profile, and cohort_placement schemas.

CREATE SCHEMA IF NOT EXISTS analytics;

-- =========================================================================
-- 1. CITIZEN SUMMARY MATERIALIZED VIEW
--    One row per citizen — aggregates auth + profile + cohort for detail view.
-- =========================================================================
CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.citizen_summary AS
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
    p.name_of_employer_business,
    p.nature_of_business_job,

    -- Early Childhood (0-5)
    p.birth_weight_kg,
    p.delivery_assistant_type,
    p.exclusive_breastfeeding_months,
    p.vaccination_record_completed,
    p.missing_vaccines,
    p.is_registered_birth,
    p.parent_guardian_nin,
    p.preschool_enrollment,

    -- Foundational Education (6-11)
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

    -- Secondary Education (12-18)
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

    -- Higher Education (19-26)
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

    -- Workforce Integration (27-32)
    p.employment_status,
    p.highest_academic_qualification,
    p.tin_number,
    p.bvn_number,
    p.monthly_income_range,
    p.nhis_enrolled,
    p.industry_sector,
    p.business_registration_number,
    p.nysc_discharge_cert,

    -- Career Growth (33-41)
    p.marital_status,
    p.dependent_children_count,
    p.housing_tenure_status,
    p.nhf_contributor,
    p.cps_pension_pin,
    p.farming_land_ownership,
    p.cooperative_membership,
    p.chronic_health_registry,

    -- Mid-Life (42-44)
    p.reskilling_interest,
    p.physical_obsolescence_risk,
    p.family_education_burden,
    p.business_expansion_capital,
    p.cardio_screening_date,
    p.primary_income_source,
    p.digital_upskilling_count,
    p.interest_in_political_office,

    -- Legacy Planning (45-48)
    p.estate_plan_will_registered,
    p.land_title_number,
    p.life_insurance_enrolled,
    p.elder_dependents_count,
    p.hypertension_diabetes_flag,
    p.secondary_income_sector,
    p.cooperative_savings_status,

    -- Pre-Retirement (49-54)
    p.expected_retirement_age,
    p.years_of_service,
    p.retirement_livelihood_plan,
    p.pfa_pension_balance_range,
    p.relocation_post_retirement,
    p.annual_geriatric_checkup,
    p.mortgage_outstanding,
    p.co_dependent_students,

    -- Active Aging (55-60)
    p.active_aging_status,
    p.senior_mentor_interest,
    p.senior_center_membership,
    p.mobility_level,
    p.primary_geriatric_clinic,
    p.nhis_senior_plan,
    p.social_support_network,

    -- Elderly Care (61+)
    p.pension_disbursement_status,
    p.primary_caregiver_name,
    p.primary_caregiver_phone,
    p.elderly_living_arrangement,
    p.adl_assistance_level,
    p.chronic_illness_diagnoses,
    p.clean_water_electricity,
    p.nassp_welfare_registered,

    p.updated_at AS profile_last_updated

FROM auth.users_auth u
LEFT JOIN citizen_profile.profiles p        ON p.citizen_code = u.citizen_code
LEFT JOIN cohort_placement.placements cp    ON cp.citizen_code = u.citizen_code
LEFT JOIN cohort_placement.cohorts c        ON c.id = cp.cohort_id
WHERE u.role = 'citizen'
WITH DATA;

-- =========================================================================
-- 2. CITIZEN STATS BY STATE + LGA MATERIALIZED VIEW
--    Pre-aggregated counts for the geographic breakdown dashboard tile.
-- =========================================================================
CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.citizen_stats_by_state_lga AS
SELECT
    u.state_of_origin::text AS state_of_origin,
    u.state_of_residence::text AS state_of_residence,
    u.lga,
    COUNT(*)                              AS total_citizens,
    COUNT(*) FILTER (WHERE u.gender::text = 'male')   AS male_count,
    COUNT(*) FILTER (WHERE u.gender::text = 'female') AS female_count,
    COUNT(*) FILTER (WHERE u.created_at >= NOW() - INTERVAL '30 days') AS registered_last_30_days,
    COUNT(*) FILTER (WHERE u.created_at >= NOW() - INTERVAL '7 days')  AS registered_last_7_days,
    MIN(u.created_at) AS first_registered_at,
    MAX(u.created_at) AS last_registered_at
FROM auth.users_auth u
WHERE u.role = 'citizen'
GROUP BY u.state_of_origin, u.state_of_residence, u.lga
WITH DATA;

-- =========================================================================
-- 3. COHORT POPULATION MATERIALIZED VIEW
--    Aggregated population and milestone stats per cohort.
-- =========================================================================
CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.cohort_population AS
SELECT
    c.id             AS cohort_id,
    c.name           AS cohort_name,
    c.min_age,
    c.max_age,
    COUNT(cp.citizen_code)                           AS total_citizens,
    COUNT(cp.citizen_code) FILTER (
        WHERE EXISTS (
            SELECT 1 FROM cohort_placement.citizen_milestones cm
            WHERE cm.citizen_code = cp.citizen_code AND cm.status = 'completed'
        )
    )                                                 AS citizens_with_any_milestone_completed,
    AVG(cp.age_at_registration)                       AS avg_age_at_registration
FROM cohort_placement.cohorts c
LEFT JOIN cohort_placement.placements cp ON cp.cohort_id = c.id
GROUP BY c.id, c.name, c.min_age, c.max_age
WITH DATA;

-- =========================================================================
-- 4. EMPLOYMENT & ECONOMIC SNAPSHOT VIEW
--    Aggregates employment-related profile data.
-- =========================================================================
CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.employment_snapshot AS
SELECT
    u.state_of_origin::text AS state_of_origin,
    u.lga,
    p.current_employment_status::text            AS employment_status,
    p.monthly_income_range,
    p.industry_sector,
    p.highest_academic_qualification,
    COUNT(*)                                     AS citizen_count
FROM auth.users_auth u
JOIN citizen_profile.profiles p ON p.citizen_code = u.citizen_code
WHERE u.role = 'citizen'
  AND p.current_employment_status IS NOT NULL
GROUP BY
    u.state_of_origin, u.lga,
    p.current_employment_status,
    p.monthly_income_range,
    p.industry_sector,
    p.highest_academic_qualification
WITH DATA;

-- =========================================================================
-- 5. HEALTH INDICATORS SNAPSHOT VIEW
-- =========================================================================
CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.health_snapshot AS
SELECT
    u.state_of_origin::text AS state_of_origin,
    u.lga,
    COUNT(*)                                                   AS total_citizens_profiled,
    COUNT(*) FILTER (WHERE p.vaccination_record_completed = true) AS vaccination_complete,
    COUNT(*) FILTER (WHERE p.is_pregnant = true)               AS pregnant_count,
    COUNT(*) FILTER (WHERE p.is_expectant_parent = true)       AS expectant_parents,
    COUNT(*) FILTER (WHERE p.hypertension_diabetes_flag = true) AS hypertension_diabetes_count,
    COUNT(*) FILTER (WHERE p.nhis_enrolled = true)             AS nhis_enrolled_count,
    COUNT(*) FILTER (WHERE p.nhis_senior_plan = true)          AS nhis_senior_plan_count,
    COUNT(*) FILTER (WHERE p.blood_group IS NOT NULL)           AS blood_group_recorded,
    COUNT(*) FILTER (WHERE p.chronic_illness_diagnoses IS NOT NULL) AS chronic_illness_count
FROM auth.users_auth u
JOIN citizen_profile.profiles p ON p.citizen_code = u.citizen_code
WHERE u.role = 'citizen'
GROUP BY u.state_of_origin, u.lga
WITH DATA;

-- =========================================================================
-- 6. PERFORMANCE INDEXES ON MATERIALIZED VIEWS
-- =========================================================================
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_citizen_summary_code
    ON analytics.citizen_summary (citizen_code);

CREATE INDEX IF NOT EXISTS idx_mv_citizen_summary_state
    ON analytics.citizen_summary (state_of_origin);

CREATE INDEX IF NOT EXISTS idx_mv_citizen_summary_lga
    ON analytics.citizen_summary (lga);

CREATE INDEX IF NOT EXISTS idx_mv_citizen_summary_cohort
    ON analytics.citizen_summary (cohort_name);

CREATE INDEX IF NOT EXISTS idx_mv_citizen_summary_gender
    ON analytics.citizen_summary (gender);

CREATE INDEX IF NOT EXISTS idx_mv_citizen_summary_registered
    ON analytics.citizen_summary (registered_at);

CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_stats_state_lga
    ON analytics.citizen_stats_by_state_lga (state_of_origin, state_of_residence, lga);

CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_cohort_pop_id
    ON analytics.cohort_population (cohort_id);

CREATE INDEX IF NOT EXISTS idx_mv_employment_state
    ON analytics.employment_snapshot (state_of_origin, lga);

CREATE INDEX IF NOT EXISTS idx_mv_health_state
    ON analytics.health_snapshot (state_of_origin, lga);
