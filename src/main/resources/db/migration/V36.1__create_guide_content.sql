CREATE TABLE IF NOT EXISTS guide_content (
    id            VARCHAR(36) NOT NULL PRIMARY KEY,
    topic_id      VARCHAR(36) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    body_html     TEXT,
    pdf_url       TEXT,
    status        VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    sort_order    INT NOT NULL DEFAULT 0,
    published_at  TIMESTAMP NULL,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL,

    CONSTRAINT fk_guide_content_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_guide_content_topic
        UNIQUE (topic_id)
);

CREATE INDEX IF NOT EXISTS idx_guide_content_status ON guide_content(status);
