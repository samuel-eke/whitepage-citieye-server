-- Migration: V12__add_missing_post_sec_pathway.sql
-- Patch migration: adds the post_sec_pathway column that was defined in the
-- Profile JPA entity but accidentally omitted from V11__add_progressive_profile_fields.sql.

ALTER TABLE citizen_profile.profiles
    ADD COLUMN IF NOT EXISTS post_sec_pathway VARCHAR(50);
