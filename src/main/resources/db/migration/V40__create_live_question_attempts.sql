-- V61__create_live_question_attempts.sql

CREATE TABLE live_question_attempts (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    session_id          VARCHAR(36)     NOT NULL,
    user_id             VARCHAR(36)     NOT NULL,
    exam_id             VARCHAR(36)     NOT NULL,
    question_id         VARCHAR(36)     NOT NULL,
    selected_option_id  VARCHAR(36),
    is_correct          BOOLEAN         NOT NULL DEFAULT FALSE,
    is_skipped          BOOLEAN         NOT NULL DEFAULT FALSE,
    answered_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lqa_session
        FOREIGN KEY (session_id) REFERENCES live_exam_sessions(id),
    CONSTRAINT fk_lqa_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_lqa_exam
        FOREIGN KEY (exam_id) REFERENCES exams(id),
    CONSTRAINT fk_lqa_question
        FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE INDEX idx_lqa_session  ON live_question_attempts (session_id);
CREATE INDEX idx_lqa_user     ON live_question_attempts (user_id);
CREATE INDEX idx_lqa_question ON live_question_attempts (question_id);
CREATE INDEX idx_lqa_exam     ON live_question_attempts (exam_id);
