-- Migration: V16__add_education_questions_fields.sql
-- Adds all primary and secondary questionnaire fields to the education.education_records table.
-- All columns are optional (nullable) as requested.

ALTER TABLE education.education_records
    -- Primary School Fields (Ages 6-11)
    ADD COLUMN IF NOT EXISTS primary_class_grade VARCHAR(20),
    ADD COLUMN IF NOT EXISTS primary_attendance_rate_tier VARCHAR(15),
    ADD COLUMN IF NOT EXISTS primary_english_literacy_pass BOOLEAN,
    ADD COLUMN IF NOT EXISTS primary_basic_numeracy_pass BOOLEAN,
    ADD COLUMN IF NOT EXISTS primary_materials_access BOOLEAN,
    ADD COLUMN IF NOT EXISTS primary_class_repeating BOOLEAN,
    ADD COLUMN IF NOT EXISTS primary_ever_dropped_out BOOLEAN,
    ADD COLUMN IF NOT EXISTS primary_school_feeding_recipient BOOLEAN,
    ADD COLUMN IF NOT EXISTS primary_absent_due_to_hunger BOOLEAN,
    ADD COLUMN IF NOT EXISTS primary_commute_distance VARCHAR(50),
    ADD COLUMN IF NOT EXISTS primary_guardian_education VARCHAR(50),
    ADD COLUMN IF NOT EXISTS primary_guardian_homework_support VARCHAR(15),
    ADD COLUMN IF NOT EXISTS primary_sibling_in_school_count INTEGER,
    ADD COLUMN IF NOT EXISTS primary_subject_strengths VARCHAR(200),
    ADD COLUMN IF NOT EXISTS primary_vocational_interest VARCHAR(50),

    -- Secondary School Fields (Ages 12-18)
    ADD COLUMN IF NOT EXISTS sec_school_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS sec_enrolled BOOLEAN,
    ADD COLUMN IF NOT EXISTS sec_attendance_rate_tier VARCHAR(15),
    ADD COLUMN IF NOT EXISTS sec_suspension_history BOOLEAN,
    ADD COLUMN IF NOT EXISTS sec_class_repeating BOOLEAN,
    ADD COLUMN IF NOT EXISTS sec_exams_registered VARCHAR(100),
    ADD COLUMN IF NOT EXISTS sec_exam_tutoring_access VARCHAR(50),
    ADD COLUMN IF NOT EXISTS sec_post_secondary_plan VARCHAR(50),
    ADD COLUMN IF NOT EXISTS sec_career_interests VARCHAR(200),
    ADD COLUMN IF NOT EXISTS sec_guardian_employment VARCHAR(50),
    ADD COLUMN IF NOT EXISTS sec_fees_paid_in_full BOOLEAN,
    ADD COLUMN IF NOT EXISTS sec_working_while_in_school BOOLEAN,
    ADD COLUMN IF NOT EXISTS sec_daily_chores_burden VARCHAR(15),
    ADD COLUMN IF NOT EXISTS sec_attendance_barriers VARCHAR(200),
    ADD COLUMN IF NOT EXISTS sec_counselor_access BOOLEAN,
    ADD COLUMN IF NOT EXISTS sec_dropout_pressure_reasons VARCHAR(200),
    ADD COLUMN IF NOT EXISTS sec_extracurricular_activities BOOLEAN,
    ADD COLUMN IF NOT EXISTS sec_digital_literacy_check BOOLEAN,
    ADD COLUMN IF NOT EXISTS sec_spoken_languages VARCHAR(200);
