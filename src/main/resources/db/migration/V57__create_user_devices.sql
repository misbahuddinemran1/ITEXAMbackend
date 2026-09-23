CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Migrate existing user_devices table to the new device model

ALTER TABLE user_devices
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(64);

ALTER TABLE user_devices
    ADD COLUMN IF NOT EXISTS device_name VARCHAR(100);

ALTER TABLE user_devices
    ADD COLUMN IF NOT EXISTS platform VARCHAR(20);

ALTER TABLE user_devices
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP;

-- Convert existing device tokens to SHA-256 hashes
UPDATE user_devices
SET token_hash = encode(
    digest(device_token, 'sha256'),
    'hex'
)
WHERE token_hash IS NULL;

-- Convert old device_type to platform
UPDATE user_devices
SET platform = device_type
WHERE platform IS NULL;

-- Convert old active status to revoked_at
UPDATE user_devices
SET revoked_at = CASE
    WHEN is_active = FALSE
        THEN COALESCE(last_used_at, created_at, NOW())
    ELSE NULL
END
WHERE revoked_at IS NULL;

-- token_hash is required by the new entity
ALTER TABLE user_devices
    ALTER COLUMN token_hash SET NOT NULL;

-- Unique token hash
ALTER TABLE user_devices
    ADD CONSTRAINT uq_user_devices_token
    UNIQUE (token_hash);

-- Replace old foreign key with ON DELETE CASCADE
ALTER TABLE user_devices
    DROP CONSTRAINT IF EXISTS fk_device_user;

ALTER TABLE user_devices
    ADD CONSTRAINT fk_user_devices_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;

-- Validate platform
ALTER TABLE user_devices
    ADD CONSTRAINT chk_user_devices_platform
    CHECK (platform IN ('ANDROID', 'IOS', 'WEB'));

-- Rename existing user_id index
ALTER INDEX IF EXISTS idx_device_user
    RENAME TO idx_user_devices_user;
