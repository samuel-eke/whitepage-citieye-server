-- Migration: V26__rename_pupil_count_to_student_count.sql
-- Description: Renames pupil_count and total_pupil_count to student_count and total_student_count in education.schools

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'pupil_count'
    ) THEN
        ALTER TABLE education.schools RENAME COLUMN pupil_count TO student_count;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'education' AND table_name = 'schools' AND column_name = 'total_pupil_count'
    ) THEN
        ALTER TABLE education.schools RENAME COLUMN total_pupil_count TO total_student_count;
    END IF;
END $$;
