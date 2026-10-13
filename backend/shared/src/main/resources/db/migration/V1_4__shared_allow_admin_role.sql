-- Allow ADMIN role
-- Band: V1
ALTER TABLE app_user DROP CONSTRAINT IF EXISTS chk_app_user_role;
ALTER TABLE app_user ADD CONSTRAINT chk_app_user_role CHECK (role IN ('CUSTOMER', 'CLIENT', 'ADMIN'));
