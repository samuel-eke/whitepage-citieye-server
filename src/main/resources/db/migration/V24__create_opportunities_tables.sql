-- Migration: V24__create_opportunities_tables.sql
-- Creates tables for storing Opportunities (programs) and citizen-specific applications.

CREATE TABLE IF NOT EXISTS admins.opportunities (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    opportunity_type VARCHAR(100) NOT NULL, -- e.g. GENERAL_WELFARE, SCHOLARSHIP
    status VARCHAR(50) NOT NULL, -- e.g. OPEN, UPCOMING, CLOSED
    brief VARCHAR(500) NOT NULL,
    full_description TEXT NOT NULL,
    required_documents VARCHAR(1000), -- Comma-separated list of required documents
    action_label VARCHAR(100) NOT NULL,
    target_cohort_id INTEGER,
    target_state_residence VARCHAR(100),
    target_state_origin VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS admins.opportunity_applications (
    id UUID PRIMARY KEY,
    citizen_code VARCHAR(30) NOT NULL REFERENCES citizen_profile.profiles(citizen_code) ON DELETE CASCADE,
    opportunity_id UUID NOT NULL REFERENCES admins.opportunities(id) ON DELETE CASCADE,
    application_status VARCHAR(50) NOT NULL, -- NOT_APPLIED, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED
    assigned_officer_code VARCHAR(30) REFERENCES auth.admin_user(citizen_code),
    officer_notes TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_citizen_opportunity UNIQUE (citizen_code, opportunity_id)
);

-- Fast lookup indexes
CREATE INDEX IF NOT EXISTS idx_opportunities_target_cohort ON admins.opportunities (target_cohort_id);
CREATE INDEX IF NOT EXISTS idx_opportunities_target_state_res ON admins.opportunities (target_state_residence);
CREATE INDEX IF NOT EXISTS idx_opportunities_target_state_orig ON admins.opportunities (target_state_origin);
CREATE INDEX IF NOT EXISTS idx_opportunity_applications_citizen ON admins.opportunity_applications (citizen_code);
CREATE INDEX IF NOT EXISTS idx_opportunity_applications_opportunity ON admins.opportunity_applications (opportunity_id);
CREATE INDEX IF NOT EXISTS idx_opportunity_applications_officer ON admins.opportunity_applications (assigned_officer_code);
