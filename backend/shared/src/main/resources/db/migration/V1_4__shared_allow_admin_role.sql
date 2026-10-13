-- Allow ADMIN role
-- Band: V1
--
-- MySQL has no `DROP CONSTRAINT ... IF EXISTS`; the conditional form this
-- migration first carried was invalid syntax and never applied anywhere. The
-- constraint is created by V1, so it is always present and the drop is
-- unconditional.
ALTER TABLE app_user DROP CHECK chk_app_user_role;
ALTER TABLE app_user ADD CONSTRAINT chk_app_user_role CHECK (role IN ('CUSTOMER', 'CLIENT', 'ADMIN'));
