-- Migration: V13__add_officer_roles.sql
-- Adds new values to the auth.user_role enum.
-- This must be in its own file to be committed before these values are used in constraints.

ALTER TYPE auth.user_role ADD VALUE IF NOT EXISTS 'field_officer_health';
ALTER TYPE auth.user_role ADD VALUE IF NOT EXISTS 'field_officer_education';
