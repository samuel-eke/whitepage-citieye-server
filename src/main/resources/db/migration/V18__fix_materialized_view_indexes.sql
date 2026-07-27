-- Migration: V18__fix_materialized_view_indexes.sql
-- Drop the non-unique indexes and create unique indexes to support CONCURRENT materialized view refreshes.

-- 1. Drop non-unique indexes
DROP INDEX IF EXISTS analytics.idx_mv_employment_state;
DROP INDEX IF EXISTS analytics.idx_mv_health_state;

-- 2. Create unique index for health_snapshot (grouped by state_of_origin and lga)
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_health_unique
    ON analytics.health_snapshot (state_of_origin, lga);

-- 3. Create unique index for employment_snapshot (grouped by all selected attributes)
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_employment_unique
    ON analytics.employment_snapshot (
        state_of_origin, 
        lga, 
        employment_status, 
        monthly_income_range, 
        industry_sector, 
        highest_academic_qualification
    );
