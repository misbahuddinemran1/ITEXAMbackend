ALTER TABLE admin_users DROP CONSTRAINT IF EXISTS chk_admin_role;

ALTER TABLE admin_users
    ADD CONSTRAINT chk_admin_role
    CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'CONTENT_MANAGER', 'REVIEWER'));
