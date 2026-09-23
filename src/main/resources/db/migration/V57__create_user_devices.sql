CREATE TABLE user_devices (
    id            VARCHAR(36)  NOT NULL DEFAULT gen_random_uuid()::text,
    user_id       VARCHAR(36)  NOT NULL,
    token_hash    VARCHAR(64)  NOT NULL,
    device_name   VARCHAR(100),
    platform      VARCHAR(20),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_used_at  TIMESTAMP,
    revoked_at    TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_user_devices_token UNIQUE (token_hash),
    CONSTRAINT fk_user_devices_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_devices_user ON user_devices (user_id);
