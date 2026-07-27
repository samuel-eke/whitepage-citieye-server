-- Migration: V15__field_officer_customizations.sql
-- Performs customizations on auth.admin_user and the citizen_code trigger.
-- Relies on roles added and committed in V13__add_officer_roles.sql.

-- 1. Drop the old check constraint restricting roles in auth.admin_user
ALTER TABLE auth.admin_user DROP CONSTRAINT IF EXISTS admin_user_role_check;

-- 2. Add new check constraint supporting specialty field officer roles
ALTER TABLE auth.admin_user
    ADD CONSTRAINT admin_user_role_check CHECK (role IN ('field_officer', 'field_officer_health', 'field_officer_education', 'agency_admin', 'super_admin'));

-- 3. Add new columns to auth.admin_user
ALTER TABLE auth.admin_user
    ADD COLUMN IF NOT EXISTS lga VARCHAR(100),
    ADD COLUMN IF NOT EXISTS date_of_birth DATE,
    ADD COLUMN IF NOT EXISTS specialty VARCHAR(50);

-- 4. Create sequence for field officer code serialization
CREATE SEQUENCE IF NOT EXISTS auth.officer_code_serial_seq START WITH 1;

-- 5. Update the citizen code auto-computation trigger function
CREATE OR REPLACE FUNCTION auth.compute_user_citizen_code()
RETURNS TRIGGER AS $$
DECLARE
    state_abbr TEXT;
    lga_prefix TEXT;
    serial_num TEXT;
    state_num TEXT;
BEGIN
    CASE NEW.role
        -- CITIZEN format: CTI-[STATE(abbrv)]-[LGA(first three letters)]-[6-DIGIT-SERIAL]
        WHEN 'citizen' THEN
            state_abbr := auth.get_state_abbreviation(NEW.state_of_origin);
            lga_prefix := UPPER(SUBSTRING(REGEXP_REPLACE(NEW.lga, '[^a-zA-Z]', '', 'g'), 1, 3));
            IF LENGTH(lga_prefix) < 3 THEN
                lga_prefix := RPAD(lga_prefix, 3, 'X');
            END IF;
            serial_num := LPAD(nextval('auth.citizen_code_serial_seq')::text, 6, '0');
            NEW.citizen_code := 'CTI-' || state_abbr || '-' || lga_prefix || '-' || serial_num;

        -- FIELD OFFICER format: CIT-OFR-[STATE OF ORIGIN]-[LGA(first three letters)]-[6-DIGIT-SERIAL]
        WHEN 'field_officer', 'field_officer_health', 'field_officer_education' THEN
            state_abbr := auth.get_state_abbreviation(NEW.state_of_origin);
            lga_prefix := UPPER(SUBSTRING(REGEXP_REPLACE(NEW.lga, '[^a-zA-Z]', '', 'g'), 1, 3));
            IF LENGTH(lga_prefix) < 3 THEN
                lga_prefix := RPAD(lga_prefix, 3, 'X');
            END IF;
            serial_num := LPAD(nextval('auth.officer_code_serial_seq')::text, 6, '0');
            NEW.citizen_code := 'CIT-OFR-' || state_abbr || '-' || lga_prefix || '-' || serial_num;

        -- AGENCY ADMIN format: cit-adm-[6 numeric digits]
        WHEN 'agency_admin' THEN
            NEW.citizen_code := 'cit-adm-' || auth.generate_random_suffix(6, TRUE);

        -- SUPER ADMIN format: cit-super-[6 numeric digits]
        WHEN 'super_admin' THEN
            NEW.citizen_code := 'cit-super-' || auth.generate_random_suffix(6, TRUE);
    END CASE;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
