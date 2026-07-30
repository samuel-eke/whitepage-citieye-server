-- Drop check constraint requiring NIN for adults or parent NIN for minors
ALTER TABLE auth.users_auth DROP CONSTRAINT IF EXISTS chk_citizen_nin_by_age;
