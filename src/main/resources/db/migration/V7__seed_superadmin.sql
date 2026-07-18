-- V7__seed_superadmin.sql
-- Flyway migration for CitiEye
-- Seeds the initial super_admin record into auth.admin_user.
--
-- ✅ Password is hashed at migration time using pgcrypto's crypt() + gen_salt('bf')
--    which was installed in the auth schema by V1__initial_db_setup.sql.
-- ✅ citizen_code is left NULL so the BEFORE INSERT trigger
--    (auth.compute_user_citizen_code) fires and generates:  cit-super-{6 numeric}
-- ⚠️  phone_number and address are required (NOT NULL) by the table definition.
--    Placeholder values are used below — update them with real data once available.
-- =========================================================================

DO $$
DECLARE
    hashed_pw TEXT;
BEGIN
    -- Only seed when the table is completely empty (fresh database)
    IF (SELECT COUNT(*) FROM auth.admin_user) = 0 THEN

        -- Hash the plain-text password using bcrypt (cost factor 12) via pgcrypto
        hashed_pw := auth.crypt('thecitieyesuperadmin', auth.gen_salt('bf', 12));

        INSERT INTO auth.admin_user (
            first_name,
            middle_name,
            last_name,
            phone_number,       -- required NOT NULL; update to real number when available
            email,
            state_of_origin,
            state_of_residence,
            gender,
            nin,
            address,            -- required NOT NULL; update to real address when available
            role,
            password,
            citizen_code        -- NULL → trigger generates 'cit-super-XXXXXX'
        ) VALUES (
            'Samuel',
            NULL,
            'Eke',
            '07043694522',      -- placeholder; replace with real phone number
            'me@admin.com',
            'Abia',
            'FCT',
            'male',
            '12345678981',
            'CitiEye HQ, Abuja, FCT',  -- placeholder; replace with real address
            'super_admin',
            hashed_pw,
            NULL
        );

        RAISE NOTICE 'Superadmin seeded successfully for: me@admin.com';
    ELSE
        RAISE NOTICE 'auth.admin_user is not empty — seed skipped.';
    END IF;
END $$;
