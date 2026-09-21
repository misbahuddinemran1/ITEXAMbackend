CREATE TABLE role_permission (
    role             VARCHAR(30) PRIMARY KEY,
    hidden_menu_ids  TEXT        NOT NULL DEFAULT '',
    updated_at       TIMESTAMP   NOT NULL DEFAULT now()
);
