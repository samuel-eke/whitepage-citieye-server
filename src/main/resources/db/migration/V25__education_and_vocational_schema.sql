-- Migration: V25__education_and_vocational_schema.sql
-- Description: Creates vocational profiles table and education tracking tables

-- 1. Extend citizen_profile.employment_status_type with 'artisan'
ALTER TYPE citizen_profile.employment_status_type ADD VALUE IF NOT EXISTS 'artisan';

-- 2. Create vocational_profiles table in citizen_profile schema
CREATE TABLE IF NOT EXISTS citizen_profile.vocational_profiles (
    id BIGSERIAL PRIMARY KEY,
    citizen_code VARCHAR(50) NOT NULL UNIQUE,
    trade_specialization VARCHAR(255),
    acquired_skills TEXT,
    proficiency_level VARCHAR(50),
    career_trajectories TEXT,
    planned_apprenticeships TEXT,
    vocational_development_goals TEXT,
    certifications TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vocational_profiles_citizen_code ON citizen_profile.vocational_profiles(citizen_code);

-- 3. Education schema tables
CREATE SCHEMA IF NOT EXISTS education;

CREATE TABLE IF NOT EXISTS education.student_academic_updates (
    id BIGSERIAL PRIMARY KEY,
    citizen_code VARCHAR(50) NOT NULL,
    academic_level VARCHAR(50) NOT NULL,
    academic_year VARCHAR(50) NOT NULL,
    institution_name VARCHAR(255),
    class_grade VARCHAR(50),
    result_card_url VARCHAR(500),
    result_file_url VARCHAR(1000),
    subject_grades JSONB,
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED', 'VERIFIED', 'ACTIVE', 'REJECTED')),
    current_status VARCHAR(50) DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_by VARCHAR(50),
    verified_at TIMESTAMP,
    notes VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_academic_updates_citizen_code ON education.student_academic_updates(citizen_code);

CREATE TABLE IF NOT EXISTS education.academic_status_transitions (
    id BIGSERIAL PRIMARY KEY,
    update_id BIGINT,
    academic_update_id BIGINT,
    citizen_code VARCHAR(50) NOT NULL,
    academic_level VARCHAR(50) NOT NULL,
    academic_year VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50),
    from_status VARCHAR(50),
    status VARCHAR(50) NOT NULL CHECK (status IN ('SUBMITTED', 'VERIFIED', 'ACTIVE', 'REJECTED')),
    to_status VARCHAR(50),
    transition_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    performed_by VARCHAR(50),
    changed_by_code VARCHAR(50),
    notes TEXT,
    remarks TEXT
);

CREATE INDEX IF NOT EXISTS idx_academic_transitions_citizen ON education.academic_status_transitions(citizen_code);
CREATE INDEX IF NOT EXISTS idx_academic_transitions_update_id ON education.academic_status_transitions(update_id);

CREATE TABLE IF NOT EXISTS education.schools (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    school_name VARCHAR(255),
    lga VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL DEFAULT 'Edo',
    pupil_count INTEGER NOT NULL CHECK (pupil_count > 0),
    total_pupil_count INTEGER,
    school_type VARCHAR(50),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS education.officer_school_assignments (
    id BIGSERIAL PRIMARY KEY,
    officer_code VARCHAR(50) NOT NULL,
    school_id BIGINT NOT NULL,
    school_name VARCHAR(255) NOT NULL,
    lga VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_officer_assignments_code ON education.officer_school_assignments(officer_code);

CREATE TABLE IF NOT EXISTS education.student_records (
    id BIGSERIAL PRIMARY KEY,
    citizen_code VARCHAR(50) NOT NULL,
    school_id BIGINT NOT NULL,
    school_name VARCHAR(255),
    lga VARCHAR(100) NOT NULL,
    student_name VARCHAR(100),
    state_of_origin VARCHAR(100),
    health_conditions_special_needs TEXT,
    diagnosed_health_conditions TEXT,
    attendance_rate NUMERIC(5,2),
    family_guardian_background TEXT,
    academic_year VARCHAR(50),
    class_grade VARCHAR(50),
    officer_code VARCHAR(50),
    logged_by_officer VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_student_records_citizen ON education.student_records(citizen_code);
CREATE INDEX IF NOT EXISTS idx_student_records_school_lga ON education.student_records(school_name, lga);
CREATE INDEX IF NOT EXISTS idx_student_records_officer ON education.student_records(officer_code);
