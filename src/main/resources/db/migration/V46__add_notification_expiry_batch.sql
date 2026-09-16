ALTER TABLE user_notifications ADD COLUMN IF NOT EXISTS expiry_date TIMESTAMP NULL;
ALTER TABLE user_notifications ADD COLUMN IF NOT EXISTS batch_id VARCHAR(36) NULL;
CREATE INDEX IF NOT EXISTS idx_notif_batch ON user_notifications (batch_id);
