-- Migration: V27__refactor_schools_table_columns.sql
-- Description: Refactors education.schools table:
--              1. Ensures school_name is populated from name before dropping name
--              2. Drops name column, leaving only school_name (NOT NULL)
--              3. Ensures total_student_count is populated from student_count before dropping student_count
--              4. Drops student_count column, leaving only total_student_count (NOT NULL CHECK (total_student_count > 0))

DO $$
BEGIN
    -- 1. If 'name' column exists and 'school_name' exists, populate school_name where null or empty
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'name'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'school_name'
    ) THEN
        UPDATE education.schools
        SET school_name = name
        WHERE (school_name IS NULL OR school_name = '') AND name IS NOT NULL;
    END IF;

    -- If 'school_name' does not exist yet (edge case), rename 'name' to 'school_name'
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'school_name'
    ) THEN
        ALTER TABLE education.schools RENAME COLUMN name TO school_name;
    END IF;

    -- Ensure school_name is NOT NULL
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'school_name'
    ) THEN
        -- Set empty names to default fallback if any row is still null
        UPDATE education.schools SET school_name = 'Unknown School' WHERE school_name IS NULL OR school_name = '';
        ALTER TABLE education.schools ALTER COLUMN school_name SET NOT NULL;
    END IF;

    -- Drop 'name' column if it still exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'name'
    ) THEN
        ALTER TABLE education.schools DROP COLUMN name;
    END IF;

    -- 2. If 'student_count' exists and 'total_student_count' exists, populate total_student_count
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'student_count'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'total_student_count'
    ) THEN
        UPDATE education.schools
        SET total_student_count = student_count
        WHERE (total_student_count IS NULL OR total_student_count <= 0) AND student_count IS NOT NULL;
    END IF;

    -- If 'total_student_count' does not exist yet, rename 'student_count' to 'total_student_count'
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'student_count'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'total_student_count'
    ) THEN
        ALTER TABLE education.schools RENAME COLUMN student_count TO total_student_count;
    END IF;

    -- Ensure total_student_count is NOT NULL and > 0
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'total_student_count'
    ) THEN
        UPDATE education.schools SET total_student_count = 1 WHERE total_student_count IS NULL OR total_student_count <= 0;
        ALTER TABLE education.schools ALTER COLUMN total_student_count SET NOT NULL;
        
        -- Drop any previous check constraints on total_student_count if existed, then add valid check
        ALTER TABLE education.schools DROP CONSTRAINT IF EXISTS chk_schools_total_student_count;
        ALTER TABLE education.schools ADD CONSTRAINT chk_schools_total_student_count CHECK (total_student_count > 0);
    END IF;

    -- Drop 'student_count' column if it still exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'student_count'
    ) THEN
        ALTER TABLE education.schools DROP COLUMN student_count;
    END IF;
END $$;
