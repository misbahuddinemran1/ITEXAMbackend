-- V55__create_guide_practice_content.sql

CREATE TABLE guide_practice_mcq (
    id                  VARCHAR(36) PRIMARY KEY,
    topic_id            VARCHAR(36) NOT NULL,
    question_text       TEXT NOT NULL,
    question_text_bn    TEXT,
    is_board_question   BOOLEAN NOT NULL DEFAULT FALSE,
    board               VARCHAR(100),
    year_appeared       INT,
    sort_order          INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_guide_practice_mcq_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
);

CREATE INDEX idx_guide_practice_mcq_topic ON guide_practice_mcq(topic_id);
CREATE INDEX idx_guide_practice_mcq_board ON guide_practice_mcq(topic_id, is_board_question);

CREATE TABLE guide_practice_mcq_option (
    id                  VARCHAR(36) PRIMARY KEY,
    mcq_id              VARCHAR(36) NOT NULL,
    option_key          VARCHAR(5) NOT NULL,
    option_text         TEXT NOT NULL,
    option_text_bn      TEXT,
    is_correct          BOOLEAN NOT NULL DEFAULT FALSE,
    explanation         TEXT,
    order_index         INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_guide_practice_mcq_option_mcq
        FOREIGN KEY (mcq_id) REFERENCES guide_practice_mcq(id) ON DELETE CASCADE
);

CREATE INDEX idx_guide_practice_mcq_option_mcq ON guide_practice_mcq_option(mcq_id);

CREATE TABLE guide_practice_cq (
    id                      VARCHAR(36) PRIMARY KEY,
    topic_id                VARCHAR(36) NOT NULL,
    stimulus                TEXT NOT NULL,
    stimulus_bn             TEXT,
    is_board_question       BOOLEAN NOT NULL DEFAULT FALSE,
    board                   VARCHAR(100),
    exam_year               INT,

    part_a_question         TEXT,
    part_a_model_answer     TEXT,
    part_a_marking_scheme   TEXT,
    part_a_max_mark         INT,

    part_b_question         TEXT,
    part_b_model_answer     TEXT,
    part_b_marking_scheme   TEXT,
    part_b_max_mark         INT,

    part_c_question         TEXT,
    part_c_model_answer     TEXT,
    part_c_marking_scheme   TEXT,
    part_c_max_mark         INT,

    part_d_question         TEXT,
    part_d_model_answer     TEXT,
    part_d_marking_scheme   TEXT,
    part_d_max_mark         INT,

    total_max_mark          INT,
    sort_order              INT NOT NULL DEFAULT 0,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_guide_practice_cq_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
);

CREATE INDEX idx_guide_practice_cq_topic ON guide_practice_cq(topic_id);
CREATE INDEX idx_guide_practice_cq_board ON guide_practice_cq(topic_id, is_board_question);
