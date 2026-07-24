-- Drop the existing foreign key constraint
ALTER TABLE auth.refresh_tokens 
    DROP CONSTRAINT IF EXISTS fk_refresh_tokens_citizen;

-- Create the shared cascade delete trigger function
CREATE OR REPLACE FUNCTION auth.cascade_delete_refresh_tokens()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM auth.refresh_tokens WHERE citizen_code = OLD.citizen_code;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Bind trigger to users_auth (citizens)
DROP TRIGGER IF EXISTS trigger_cascade_delete_refresh_tokens_users ON auth.users_auth;
CREATE TRIGGER trigger_cascade_delete_refresh_tokens_users
AFTER DELETE ON auth.users_auth
FOR EACH ROW EXECUTE FUNCTION auth.cascade_delete_refresh_tokens();

-- Bind trigger to admin_user (admins, super admins, field officers)
DROP TRIGGER IF EXISTS trigger_cascade_delete_refresh_tokens_admins ON auth.admin_user;
CREATE TRIGGER trigger_cascade_delete_refresh_tokens_admins
AFTER DELETE ON auth.admin_user
FOR EACH ROW EXECUTE FUNCTION auth.cascade_delete_refresh_tokens();
