create schema if not exists auth;

-- =========================================================================
-- 1. SCHEMA & EXTENSIONS & ENUMS DEFINITIONS
-- =========================================================================

-- Enable pgcrypto in the auth schema
CREATE EXTENSION IF NOT EXISTS pgcrypto SCHEMA auth;

-- Define Gender Enum in the auth schema
CREATE TYPE auth.gender_type AS ENUM ('male', 'female');

-- Define System Roles Enum in the auth schema
CREATE TYPE auth.user_role AS ENUM ('citizen', 'field_officer', 'agency_admin', 'super_admin');

-- Define Nigerian States Enum in the auth schema (Ordered alphabetically to match numeric coding)
CREATE TYPE auth.nigerian_state AS ENUM (
    'Abia', 'Adamawa', 'Akwa Ibom', 'Anambra', 'Bauchi', 'Bayelsa', 'Benue', 'Borno',
    'Cross River', 'Delta', 'Ebonyi', 'Edo', 'Ekiti', 'Enugu', 'Gombe', 'Imo',
    'Jigawa', 'Kaduna', 'Kano', 'Katsina', 'Kebbi', 'Kogi', 'Kwara', 'Lagos',
    'Nasarawa', 'Niger', 'Ogun', 'Ondo', 'Osun', 'Oyo', 'Plateau', 'Rivers',
    'Sokoto', 'Taraba', 'Yobe', 'Zamfara', 'FCT'
);

-- =========================================================================
-- 2. HELPER FUNCTIONS FOR CITIZEN CODE GENERATION
-- =========================================================================

-- Helper function to map a State Enum to its 2-digit alphabetical number (in auth schema)
CREATE OR REPLACE FUNCTION auth.get_state_numeric_code(state_val auth.nigerian_state)
RETURNS TEXT AS $$
BEGIN
    RETURN lpad(CASE state_val
        WHEN 'Abia' THEN 1 WHEN 'Adamawa' THEN 2 WHEN 'Akwa Ibom' THEN 3 WHEN 'Anambra' THEN 4
        WHEN 'Bauchi' THEN 5 WHEN 'Bayelsa' THEN 6 WHEN 'Benue' THEN 7 WHEN 'Borno' THEN 8
        WHEN 'Cross River' THEN 9 WHEN 'Delta' THEN 10 WHEN 'Ebonyi' THEN 11 WHEN 'Edo' THEN 12
        WHEN 'Ekiti' THEN 13 WHEN 'Enugu' THEN 14 WHEN 'Gombe' THEN 15 WHEN 'Imo' THEN 16
        WHEN 'Jigawa' THEN 17 WHEN 'Kaduna' THEN 18 WHEN 'Kano' THEN 19 WHEN 'Katsina' THEN 20
        WHEN 'Kebbi' THEN 21 WHEN 'Kogi' THEN 22 WHEN 'Kwara' THEN 23 WHEN 'Lagos' THEN 24
        WHEN 'Nasarawa' THEN 25 WHEN 'Niger' THEN 26 WHEN 'Ogun' THEN 27 WHEN 'Ondo' THEN 28
        WHEN 'Osun' THEN 29 WHEN 'Oyo' THEN 30 WHEN 'Plateau' THEN 31 WHEN 'Rivers' THEN 32
        WHEN 'Sokoto' THEN 33 WHEN 'Taraba' THEN 34 WHEN 'Yobe' THEN 35 WHEN 'Zamfara' THEN 36
        WHEN 'FCT' THEN 37
    END::text, 2, '0');
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Helper function to map a State Enum to its standard abbreviation (in auth schema)
CREATE OR REPLACE FUNCTION auth.get_state_abbreviation(state_val auth.nigerian_state)
RETURNS TEXT AS $$
BEGIN
    RETURN CASE state_val
        WHEN 'Abia' THEN 'AB' WHEN 'Adamawa' THEN 'AD' WHEN 'Akwa Ibom' THEN 'AK' WHEN 'Anambra' THEN 'AN'
        WHEN 'Bauchi' THEN 'BA' WHEN 'Bayelsa' THEN 'BY' WHEN 'Benue' THEN 'BE' WHEN 'Borno' THEN 'BO'
        WHEN 'Cross River' THEN 'CR' WHEN 'Delta' THEN 'DE' WHEN 'Ebonyi' THEN 'EB' WHEN 'Edo' THEN 'ED'
        WHEN 'Ekiti' THEN 'EK' WHEN 'Enugu' THEN 'EN' WHEN 'Gombe' THEN 'GO' WHEN 'Imo' THEN 'IM'
        WHEN 'Jigawa' THEN 'JI' WHEN 'Kaduna' THEN 'KD' WHEN 'Kano' THEN 'KN' WHEN 'Katsina' THEN 'KT'
        WHEN 'Kebbi' THEN 'KE' WHEN 'Kogi' THEN 'KO' WHEN 'Kwara' THEN 'KW' WHEN 'Lagos' THEN 'LA'
        WHEN 'Nasarawa' THEN 'NA' WHEN 'Niger' THEN 'NI' WHEN 'Ogun' THEN 'OG' WHEN 'Ondo' THEN 'ON'
        WHEN 'Osun' THEN 'OS' WHEN 'Oyo' THEN 'OY' WHEN 'Plateau' THEN 'PL' WHEN 'Rivers' THEN 'RI'
        WHEN 'Sokoto' THEN 'SO' WHEN 'Taraba' THEN 'TA' WHEN 'Yobe' THEN 'YO' WHEN 'Zamfara' THEN 'ZA'
        WHEN 'FCT' THEN 'FC'
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Helper function to generate random alphanumeric or purely numeric strings (in auth schema)
CREATE OR REPLACE FUNCTION auth.generate_random_suffix(suffix_length INT, numeric_only BOOLEAN DEFAULT FALSE)
RETURNS TEXT AS $$
DECLARE
    allowed_chars TEXT := 'abcdef0123456789';
    result TEXT := '';
    i INT;
BEGIN
    IF numeric_only THEN
        allowed_chars := '0123456789';
    END IF;

    FOR i IN 1..suffix_length LOOP
        result := result || substr(allowed_chars, floor(random() * length(allowed_chars))::int + 1, 1);
    END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql VOLATILE;

-- Sequence for custom citizen code serial generation
CREATE SEQUENCE IF NOT EXISTS auth.citizen_code_serial_seq START WITH 1 INCREMENT BY 1;

-- =========================================================================
-- 3. TABLES DEFINITION (1:1 Relationship via citizen_code)
-- =========================================================================

-- The Authentication and Core Identity Table in auth schema
CREATE TABLE auth.users_auth (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,

    -- Optional phone number
    phone_number VARCHAR(11) UNIQUE,

    -- Optional email
    email VARCHAR(255) UNIQUE,
    date_of_birth DATE NOT NULL,
    state_of_residence auth.nigerian_state NOT NULL,
    state_of_origin auth.nigerian_state NOT NULL,
    
    -- LGA is required for citizens (used for custom citizen_code generation)
    lga VARCHAR(100),
    gender auth.gender_type NOT NULL,

    -- Optional NIN (but constrained by age check below)
    nin VARCHAR(11) UNIQUE,
    
    -- Optional Parent's NIN for minors
    parent_nin VARCHAR(11),

    address TEXT NOT NULL,
    role auth.user_role NOT NULL,

    -- Minimum password length constraint of 8 characters
    password VARCHAR(255) NOT NULL CHECK (char_length(password) >= 8),

    -- The citizen_code will be automatically generated and made unique
    citizen_code VARCHAR(30) UNIQUE,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_phone_number CHECK (phone_number IS NULL OR phone_number ~ '^0[0-9]{10}$'),
    CONSTRAINT chk_email CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}$'),
    CONSTRAINT chk_nin CHECK (nin IS NULL OR nin ~ '^[0-9]{11}$'),
    CONSTRAINT chk_parent_nin CHECK (parent_nin IS NULL OR parent_nin ~ '^[0-9]{11}$'),
    CONSTRAINT chk_citizen_lga CHECK (role != 'citizen' OR lga IS NOT NULL),
    
    -- Constraint enforcing NIN rules: Adults (age >= 18) must have own NIN, minors must have own or parent's NIN.
    CONSTRAINT chk_citizen_nin_by_age CHECK (
        role != 'citizen' OR
        (date_of_birth <= (created_at::date - INTERVAL '18 years') AND nin IS NOT NULL) OR
        (date_of_birth > (created_at::date - INTERVAL '18 years') AND (nin IS NOT NULL OR parent_nin IS NOT NULL))
    )
);

-- =========================================================================
-- 4. TRIGGER FOR COMPUTE-ON-INSERT CITIZEN CODE LOGIC
-- =========================================================================

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

        -- FIELD OFFICER format: cit-fo-[state_of_residence_numeric]-[6 alphanumeric]
        WHEN 'field_officer' THEN
            state_num := auth.get_state_numeric_code(NEW.state_of_residence);
            NEW.citizen_code := 'cit-fo-' || state_num || '-' || auth.generate_random_suffix(6, FALSE);

        -- AGENCY ADMIN format: cit-adm-[6 numeric digits]
        WHEN 'agency_admin' THEN
            NEW.citizen_code := 'cit-adm-' || auth.generate_random_suffix(6, TRUE);

        -- SUPER ADMIN fallback handler (Keeps patterns uniform if a super admin is registered)
        WHEN 'super_admin' THEN
            NEW.citizen_code := 'cit-super-' || auth.generate_random_suffix(6, TRUE);
    END CASE;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Bind trigger to run BEFORE an insert happens on auth.users_auth
CREATE TRIGGER trigger_auto_compute_citizen_code
BEFORE INSERT ON auth.users_auth
FOR EACH ROW
EXECUTE FUNCTION auth.compute_user_citizen_code();

-- =========================================================================
-- 5. PERFORMANCE INDEXES FOR MILLIONS OF USERS
-- =========================================================================
CREATE INDEX idx_users_auth_email_lower ON auth.users_auth (LOWER(email));
CREATE INDEX idx_users_auth_role ON auth.users_auth (role);




BEGIN;

-- =========================================================================
-- 1. SCHEMA & ENUMS DEFINITIONS
-- =========================================================================

-- Create the citizen_profile schema if it does not exist
CREATE SCHEMA IF NOT EXISTS citizen_profile;

-- Define Employment Status Enum in the citizen_profile schema
CREATE TYPE citizen_profile.employment_status_type AS ENUM (
    'employed',
    'unemployed',
    'business_owner'
    );

-- =========================================================================
-- 2. TABLE DEFINITION (1:1 Relationship with auth.users_auth)
-- =========================================================================

CREATE TABLE citizen_profile.profiles (
    -- Primary Key and Foreign Key pointing to the auth.users_auth table
                                          citizen_code VARCHAR(30) PRIMARY KEY REFERENCES auth.users_auth(citizen_code) ON DELETE CASCADE,

    -- Optional fields
                                          lga VARCHAR(100),
                                          hometown VARCHAR(100),
                                          current_educational_level VARCHAR(100),
                                          highest_educational_qualification VARCHAR(100),
                                          course_of_study VARCHAR(100),

    -- Calculated and updated automatically from auth.users_auth(date_of_birth) via trigger
                                          age INT CHECK (age >= 0),

                                          university_attended VARCHAR(255),
                                          degree_attained VARCHAR(100),
                                          year_of_graduation INT CHECK (year_of_graduation BETWEEN 1900 AND 2100),

                                          is_expectant_parent BOOLEAN, -- expectant mother/father
                                          household_size INT CHECK (household_size >= 0),
                                          is_pregnant BOOLEAN, -- enrolled for antenatal

                                          blood_group VARCHAR(5),    -- e.g., 'A+', 'O-', 'B+'
                                          blood_genotype VARCHAR(5), -- e.g., 'AA', 'AS', 'SS'

                                          current_employment_status citizen_profile.employment_status_type,
                                          place_of_employment_business VARCHAR(255),
                                          name_of_employer_business VARCHAR(255),
                                          nature_of_business_job VARCHAR(255),

                                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- =========================================================================
-- 3. TRIGGER TO AUTOMATICALLY CALCULATE & SYNC AGE FROM DOB
-- =========================================================================

-- Trigger function to fetch DOB from auth.users_auth and calculate current age
CREATE OR REPLACE FUNCTION citizen_profile.sync_profile_age()
    RETURNS TRIGGER AS $$
DECLARE
    user_dob DATE;
BEGIN
    -- Fetch the DOB of the user matching the profile's citizen_code
    SELECT date_of_birth INTO user_dob
    FROM auth.users_auth
    WHERE citizen_code = NEW.citizen_code;

    -- If DOB is found, calculate the age in years
    IF user_dob IS NOT NULL THEN
        NEW.age := date_part('year', age(user_dob))::integer;
    ELSE
        NEW.age := NULL;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Bind the trigger to run BEFORE INSERT or UPDATE on citizen_profile.profiles
CREATE TRIGGER trigger_calculate_profile_age
    BEFORE INSERT OR UPDATE ON citizen_profile.profiles
    FOR EACH ROW
EXECUTE FUNCTION citizen_profile.sync_profile_age();

-- =========================================================================
-- 4. PERFORMANCE INDEXES
-- =========================================================================
CREATE INDEX idx_profiles_employment ON citizen_profile.profiles (current_employment_status);

COMMIT;




BEGIN;

-- =========================================================================
-- 1. SCHEMA DEFINITION
-- =========================================================================
CREATE SCHEMA IF NOT EXISTS cohort_placement;

-- =========================================================================
-- 2. COHORTS REFERENCE TABLE
-- =========================================================================
CREATE TABLE cohort_placement.cohorts (
                                          id SERIAL PRIMARY KEY,
                                          name VARCHAR(100) UNIQUE NOT NULL,
                                          min_age INT NOT NULL CHECK (min_age >= 0),
                                          max_age INT CHECK (max_age >= min_age), -- NULL represents 61+
                                          description TEXT,
                                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Populate the 11 target cohorts
INSERT INTO cohort_placement.cohorts (name, min_age, max_age, description) VALUES
                                                                               ('Early Childhood Development', 0, 5, 'Immunizations, nutrition tracking, and early childhood care.'),
                                                                               ('Foundational Education', 6, 11, 'Primary school enrollment, literacy, numeracy, and basic vaccinations.'),
                                                                               ('Secondary Education & Career Awareness', 12, 18, 'Secondary school completion, adolescent health, and career exposure.'),
                                                                               ('Higher Education & Skill Acquisition', 19, 26, 'Vocational training, college education, and tertiary skills.'),
                                                                               ('Workforce Integration', 27, 32, 'Job matching, tax registration, entrepreneurship support, and business capital.'),
                                                                               ('Career Growth & Family Stability', 33, 41, 'Homeownership, family health insurance, and professional development.'),
                                                                               ('Mid-Life Reinvention', 42, 44, 'Career pivoting, health screening, and digital literacy skills.'),
                                                                               ('Legacy & Stability Planning', 45, 48, 'Pensions planning, asset accumulation, and chronic illness screenings.'),
                                                                               ('Pre-Retirement Transition', 49, 54, 'Retirement readiness courses, transition planning, and physical therapy.'),
                                                                               ('Active Aging', 55, 60, 'Community mentorship, wellness classes, and light work consultancies.'),
                                                                               ('Elderly Care & Support', 61, NULL, 'Specialized geriatric care, social welfare stipends, and elderly assistance.');

-- =========================================================================
-- 3. CURRENT PLACEMENTS TABLE (1:1 with auth.users_auth)
-- =========================================================================
CREATE TABLE cohort_placement.placements (
                                             citizen_code VARCHAR(30) PRIMARY KEY REFERENCES auth.users_auth(citizen_code) ON DELETE CASCADE,
                                             cohort_id INT NOT NULL REFERENCES cohort_placement.cohorts(id) ON DELETE RESTRICT,
                                             age_at_registration INT NOT NULL CHECK (age_at_registration >= 0),
                                             last_transitioned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- =========================================================================
-- 4. HISTORICAL PLACEMENT LOGS (Audits all transitions)
-- =========================================================================
CREATE TABLE cohort_placement.placement_history (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    citizen_code VARCHAR(30) NOT NULL REFERENCES auth.users_auth(citizen_code) ON DELETE CASCADE,
                                                    previous_cohort_id INT REFERENCES cohort_placement.cohorts(id) ON DELETE SET NULL,
                                                    new_cohort_id INT NOT NULL REFERENCES cohort_placement.cohorts(id) ON DELETE RESTRICT,
                                                    transition_reason VARCHAR(100) NOT NULL, -- 'registration', 'birthday_transition'
                                                    transitioned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- =========================================================================
-- 5. MILESTONE TEMPLATES & TRACKING TABLES
-- =========================================================================

-- Defines the master milestones for each cohort
CREATE TABLE cohort_placement.milestone_templates (
                                                      id SERIAL PRIMARY KEY,
                                                      cohort_id INT NOT NULL REFERENCES cohort_placement.cohorts(id) ON DELETE CASCADE,
                                                      title VARCHAR(255) NOT NULL,
                                                      description TEXT,
                                                      is_mandatory BOOLEAN DEFAULT TRUE NOT NULL,
    -- Optional JSON Schema defining validation criteria (e.g. must upload file, need school name)
                                                      meta_schema JSONB,
                                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Tracks citizen completion of milestones
CREATE TABLE cohort_placement.citizen_milestones (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     citizen_code VARCHAR(30) NOT NULL REFERENCES auth.users_auth(citizen_code) ON DELETE CASCADE,
                                                     milestone_template_id INT NOT NULL REFERENCES cohort_placement.milestone_templates(id) ON DELETE CASCADE,
                                                     status VARCHAR(50) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'in_progress', 'completed', 'waived')),
                                                     assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                                     completed_at TIMESTAMP WITH TIME ZONE,
    -- Stores specific submission data (e.g., {"school_name": "Lagos Academy", "file_url": "..."})
                                                     verification_data JSONB,
                                                     last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                                     UNIQUE (citizen_code, milestone_template_id)
);

-- =========================================================================
-- 6. KPI TEMPLATES & TRACKING TABLES
-- =========================================================================

-- Defines the master KPIs for each cohort
CREATE TABLE cohort_placement.kpi_templates (
                                                id SERIAL PRIMARY KEY,
                                                cohort_id INT NOT NULL REFERENCES cohort_placement.cohorts(id) ON DELETE CASCADE,
                                                title VARCHAR(255) NOT NULL,
                                                description TEXT,
                                                metric_type VARCHAR(50) NOT NULL CHECK (metric_type IN ('numeric', 'boolean', 'percentage', 'scale')),
    -- Target definition (e.g., {"target": 90, "operator": ">="} or {"target": true})
                                                target_criteria JSONB NOT NULL,
                                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Tracks citizen performance against cohort KPIs
CREATE TABLE cohort_placement.citizen_kpis (
                                               id BIGSERIAL PRIMARY KEY,
                                               citizen_code VARCHAR(30) NOT NULL REFERENCES auth.users_auth(citizen_code) ON DELETE CASCADE,
                                               kpi_template_id INT NOT NULL REFERENCES cohort_placement.kpi_templates(id) ON DELETE CASCADE,
    -- Current progress metrics (e.g., {"value": 85.5} or {"value": true})
                                               current_value JSONB NOT NULL DEFAULT '{}'::jsonb,
                                               status VARCHAR(50) NOT NULL DEFAULT 'not_started' CHECK (status IN ('not_started', 'in_progress', 'met', 'failed')),
    -- Audit trail of metric updates
                                               history_log JSONB DEFAULT '[]'::jsonb,
                                               last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                               UNIQUE (citizen_code, kpi_template_id)
);

-- =========================================================================
-- 7. TRIGGER FOR AUTOMATIC PLACEMENT AT REGISTRATION
-- =========================================================================

CREATE OR REPLACE FUNCTION cohort_placement.assign_citizen_cohort()
    RETURNS TRIGGER AS $$
DECLARE
    reg_age INT;
    matched_cohort_id INT;
BEGIN
    -- Only assign to cohort if the registered user is a citizen
    IF NEW.role = 'citizen' THEN
        -- Calculate age in years at registration time
        reg_age := date_part('year', age(NEW.created_at, NEW.date_of_birth))::integer;

        -- Find the matching cohort ID
        SELECT id INTO matched_cohort_id
        FROM cohort_placement.cohorts
        WHERE reg_age >= min_age AND (max_age IS NULL OR reg_age <= max_age)
        LIMIT 1;

        -- Place the citizen in the calculated cohort
        IF matched_cohort_id IS NOT NULL THEN
            -- 1. Create Placement
            INSERT INTO cohort_placement.placements (citizen_code, cohort_id, age_at_registration)
            VALUES (NEW.citizen_code, matched_cohort_id, reg_age);

            -- 2. Log Initial Transition History
            INSERT INTO cohort_placement.placement_history (citizen_code, previous_cohort_id, new_cohort_id, transition_reason)
            VALUES (NEW.citizen_code, NULL, matched_cohort_id, 'registration');
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Bind the trigger (Must run AFTER INSERT on auth.users_auth)
CREATE TRIGGER trigger_auto_assign_cohort
    AFTER INSERT ON auth.users_auth
    FOR EACH ROW
EXECUTE FUNCTION cohort_placement.assign_citizen_cohort();

-- =========================================================================
-- 8. INDEXES FOR PERFORMANCE
-- =========================================================================
CREATE INDEX idx_placements_cohort ON cohort_placement.placements (cohort_id);
CREATE INDEX idx_placement_history_code ON cohort_placement.placement_history (citizen_code);
CREATE INDEX idx_citizen_milestones_status ON cohort_placement.citizen_milestones (status);
CREATE INDEX idx_citizen_kpis_status ON cohort_placement.citizen_kpis (status);

-- GIN indexes for fast searching inside JSONB columns
CREATE INDEX idx_milestone_templates_schema ON cohort_placement.milestone_templates USING gin (meta_schema);
CREATE INDEX idx_citizen_milestones_verif ON cohort_placement.citizen_milestones USING gin (verification_data);
CREATE INDEX idx_citizen_kpis_value ON cohort_placement.citizen_kpis USING gin (current_value);

COMMIT;
