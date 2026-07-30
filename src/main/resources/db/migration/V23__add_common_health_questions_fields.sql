-- Migration: V23__add_common_health_questions_fields.sql
-- Adds common health questionnaire fields for all citizens to the health.health_records table.

ALTER TABLE health.health_records
    ADD COLUMN IF NOT EXISTS common_blood_group VARCHAR(5),
    ADD COLUMN IF NOT EXISTS common_blood_genotype VARCHAR(5),
    ADD COLUMN IF NOT EXISTS common_is_expectant_parent BOOLEAN,
    ADD COLUMN IF NOT EXISTS common_is_pregnant BOOLEAN,
    ADD COLUMN IF NOT EXISTS common_history_of_diabetes BOOLEAN,
    ADD COLUMN IF NOT EXISTS common_hereditary_disease VARCHAR(255),
    ADD COLUMN IF NOT EXISTS common_nhis_enrolled BOOLEAN,
    ADD COLUMN IF NOT EXISTS common_nhis_senior_plan_active BOOLEAN;
