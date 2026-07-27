-- Migration: V14__health_education_schemas.sql
-- Creates dedicated PostgreSQL schemas and tables for Health and Education records.

-- 1. Schemas
CREATE SCHEMA IF NOT EXISTS health;
CREATE SCHEMA IF NOT EXISTS education;

-- 2. Health Records Table
CREATE TABLE IF NOT EXISTS health.health_records (
    id BIGSERIAL PRIMARY KEY,
    citizen_code VARCHAR(30) NOT NULL REFERENCES citizen_profile.profiles(citizen_code) ON DELETE CASCADE,
    officer_code VARCHAR(30) NOT NULL REFERENCES auth.admin_user(citizen_code),
    record_type VARCHAR(50) NOT NULL, -- VACCINATION, SCREENING, NUTRITION
    description TEXT NOT NULL,
    notes TEXT,
    logged_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. Education Records Table
CREATE TABLE IF NOT EXISTS education.education_records (
    id BIGSERIAL PRIMARY KEY,
    citizen_code VARCHAR(30) NOT NULL REFERENCES citizen_profile.profiles(citizen_code) ON DELETE CASCADE,
    officer_code VARCHAR(30) NOT NULL REFERENCES auth.admin_user(citizen_code),
    record_type VARCHAR(50) NOT NULL, -- ENROLLMENT, EXAM_RESULT, ATTENDANCE
    institution_name VARCHAR(150) NOT NULL,
    academic_year VARCHAR(15) NOT NULL,
    class_grade VARCHAR(20),
    subject_grades JSONB, -- stores key-value pairs (e.g. {"Math": "A1"})
    attendance_rate DECIMAL(5,2),
    logged_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 4. Indexes for fast query lookup (Using IF NOT EXISTS to prevent migration retry errors)
CREATE INDEX IF NOT EXISTS idx_health_records_citizen ON health.health_records(citizen_code);
CREATE INDEX IF NOT EXISTS idx_health_records_officer ON health.health_records(officer_code);
CREATE INDEX IF NOT EXISTS idx_education_records_citizen ON education.education_records(citizen_code);
CREATE INDEX IF NOT EXISTS idx_education_records_officer ON education.education_records(officer_code);
