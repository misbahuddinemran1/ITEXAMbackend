-- V41__add_institution_session_to_users.sql
-- User entity-তে institutionName ও session ফিল্ড আছে কিন্তু migration-এ কখনো যোগ হয়নি

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS institution_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS session VARCHAR(20);
