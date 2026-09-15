-- =========================================
-- V37: Doubt Solver Module
-- Doubt Question, Doubt Answer
-- =========================================

-- 1. DOUBT QUESTION
CREATE TABLE doubt_question (
    id                      VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    student_user_id         VARCHAR(36)   NOT NULL,
    subject_id              VARCHAR(36),
    chapter_id              VARCHAR(36)   NOT NULL,
    topic_id                VARCHAR(36),
    question_text           TEXT,
    question_image_url      VARCHAR(500),
    question_pdf_url        VARCHAR(500),
    status                  VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING','REVIEWED','ANSWERED')),
    reviewed_by_admin_id     VARCHAR(36),
    reviewed_at              TIMESTAMP,
    created_at               TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_doubt_question_student
        FOREIGN KEY (student_user_id) REFERENCES users(id),
    CONSTRAINT fk_doubt_question_subject
        FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_doubt_question_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT fk_doubt_question_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_doubt_question_admin
        FOREIGN KEY (reviewed_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_doubt_question_status  ON doubt_question (status);
CREATE INDEX idx_doubt_question_student ON doubt_question (student_user_id);
CREATE INDEX idx_doubt_question_chapter ON doubt_question (chapter_id);
CREATE INDEX idx_doubt_question_subject ON doubt_question (subject_id);


-- 2. DOUBT ANSWER
CREATE TABLE doubt_answer (
    id                  VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    doubt_question_id   VARCHAR(36)   NOT NULL,
    admin_id            VARCHAR(36)   NOT NULL,
    answer_text         TEXT,
    answer_pdf_url      VARCHAR(500),
    answered_via_ai     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_doubt_answer_question
        FOREIGN KEY (doubt_question_id) REFERENCES doubt_question(id),
    CONSTRAINT fk_doubt_answer_admin
        FOREIGN KEY (admin_id) REFERENCES admin_users(id),
    CONSTRAINT uk_doubt_answer_question UNIQUE (doubt_question_id)
);

CREATE INDEX idx_doubt_answer_question ON doubt_answer (doubt_question_id);


-- 3. updated_at auto triggers
DROP TRIGGER IF EXISTS trg_doubt_question_updated_at ON doubt_question;
CREATE TRIGGER trg_doubt_question_updated_at
BEFORE UPDATE ON doubt_question
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_doubt_answer_updated_at ON doubt_answer;
CREATE TRIGGER trg_doubt_answer_updated_at
BEFORE UPDATE ON doubt_answer
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
