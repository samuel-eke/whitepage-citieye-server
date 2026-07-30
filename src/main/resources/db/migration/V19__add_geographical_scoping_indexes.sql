-- Migration: V19__add_geographical_scoping_indexes.sql
-- Description: Adds indexes on state_of_residence for performance optimization of geographically-scoped queries.

-- Index on users_auth for citizen listing/filtering
CREATE INDEX IF NOT EXISTS idx_users_auth_state_residence ON auth.users_auth (state_of_residence);

-- Index on citizen_summary materialized view
CREATE INDEX IF NOT EXISTS idx_mv_citizen_summary_residence ON analytics.citizen_summary (state_of_residence);
