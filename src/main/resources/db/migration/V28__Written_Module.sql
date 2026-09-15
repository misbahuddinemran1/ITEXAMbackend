-- =========================================
-- V29: Written Module
-- Exam, Question, Submission, Evaluation, Practice, Settings, History
-- =========================================

-- 1. WRITTEN EXAM
CREATE TABLE written_exam (
    id                  VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    title               VARCHAR(200)  NOT NULL,
    title_bn            VARCHAR(200),
    description         TEXT,
    education_level     VARCHAR(10)   CHECK (education_level IN ('SSC','HSC','HONORS','MASTERS','OTHER')),
    subject_id          VARCHAR(36),
    chapter_id          VARCHAR(36),
    topic_id            VARCHAR(36),
    total_marks         INT           NOT NULL DEFAULT 0,
    duration_minutes    INT           NOT NULL DEFAULT 0,
    start_time          TIMESTAMP,
    end_time            TIMESTAMP,
    cycle_number        INT           NOT NULL DEFAULT 1,
    status              VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT','PUBLISHED','LIVE','ENDED','ARCHIVED')),
    evaluation_mode     VARCHAR(10)   NOT NULL DEFAULT 'MANUAL'
                            CHECK (evaluation_mode IN ('MANUAL','AI','HYBRID')),
    ai_provider         VARCHAR(10)   CHECK (ai_provider IN ('GEMINI','CLAUDE','OPENAI')),
    part_a_mode         VARCHAR(10)   NOT NULL DEFAULT 'MANUAL' CHECK (part_a_mode IN ('AI','MANUAL')),
    part_b_mode         VARCHAR(10)   NOT NULL DEFAULT 'MANUAL' CHECK (part_b_mode IN ('AI','MANUAL')),
    part_c_mode         VARCHAR(10)   NOT NULL DEFAULT 'MANUAL' CHECK (part_c_mode IN ('AI','MANUAL')),
    part_d_mode         VARCHAR(10)   NOT NULL DEFAULT 'MANUAL' CHECK (part_d_mode IN ('AI','MANUAL')),
    created_by_admin_id VARCHAR(36),
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_exam_admin
        FOREIGN KEY (created_by_admin_id) REFERENCES admin_users(id),
    CONSTRAINT fk_written_exam_subject
        FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_written_exam_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT fk_written_exam_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id)
);

CREATE INDEX idx_written_exam_status     ON written_exam (status);
CREATE INDEX idx_written_exam_level      ON written_exam (education_level);
CREATE INDEX idx_written_exam_start_time ON written_exam (start_time);
CREATE INDEX idx_written_exam_subject    ON written_exam (subject_id);


-- 2. WRITTEN QUESTION (CQ)
CREATE TABLE written_question (
    id                    VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    exam_id               VARCHAR(36)   NOT NULL,
    subject_id            VARCHAR(36)   NOT NULL,
    chapter_id            VARCHAR(36)   NOT NULL,
    topic_id              VARCHAR(36)   NOT NULL,
    question_order        INT           NOT NULL DEFAULT 1,
    stimulus              TEXT          NOT NULL,
    stimulus_bn           TEXT,

    part_a_question       TEXT          NOT NULL,
    part_a_model_answer   TEXT,
    part_a_ai_answer      TEXT,
    part_a_marking_scheme TEXT,
    part_a_max_mark       DECIMAL(5,2)  NOT NULL DEFAULT 1.00,

    part_b_question       TEXT          NOT NULL,
    part_b_model_answer   TEXT,
    part_b_ai_answer      TEXT,
    part_b_marking_scheme TEXT,
    part_b_max_mark       DECIMAL(5,2)  NOT NULL DEFAULT 2.00,

    part_c_question       TEXT          NOT NULL,
    part_c_model_answer   TEXT,
    part_c_ai_answer      TEXT,
    part_c_marking_scheme TEXT,
    part_c_max_mark       DECIMAL(5,2)  NOT NULL DEFAULT 3.00,

    part_d_question       TEXT          NOT NULL,
    part_d_model_answer   TEXT,
    part_d_ai_answer      TEXT,
    part_d_marking_scheme TEXT,
    part_d_max_mark       DECIMAL(5,2)  NOT NULL DEFAULT 4.00,

    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_question_exam
        FOREIGN KEY (exam_id) REFERENCES written_exam(id),
    CONSTRAINT fk_written_question_subject
        FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_written_question_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT fk_written_question_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id)
);

CREATE INDEX idx_written_question_exam    ON written_question (exam_id);
CREATE INDEX idx_written_question_subject ON written_question (subject_id);
CREATE INDEX idx_written_question_topic   ON written_question (topic_id);


-- 3. WRITTEN SUBMISSION
CREATE TABLE written_submission (
    id                  VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    exam_id             VARCHAR(36)   NOT NULL,
    user_id             VARCHAR(36)   NOT NULL,
    cycle_number        INT           NOT NULL DEFAULT 1,
    attempt_number      INT           NOT NULL DEFAULT 1,
    status              VARCHAR(20)   NOT NULL DEFAULT 'NOT_STARTED'
                            CHECK (status IN ('NOT_STARTED','IN_PROGRESS','SUBMITTED','UNDER_REVIEW','COMPLETED')),
    started_at          TIMESTAMP,
    submitted_at        TIMESTAMP,
    total_obtained_mark DECIMAL(6,2),
    is_practice_mode    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_submission_exam
        FOREIGN KEY (exam_id) REFERENCES written_exam(id),
    CONSTRAINT fk_written_submission_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX uk_written_submission_live_cycle
    ON written_submission (exam_id, user_id, cycle_number)
    WHERE is_practice_mode = FALSE;

CREATE INDEX idx_written_submission_exam   ON written_submission (exam_id);
CREATE INDEX idx_written_submission_user   ON written_submission (user_id);
CREATE INDEX idx_written_submission_status ON written_submission (status);
CREATE INDEX idx_written_submission_cycle  ON written_submission (exam_id, user_id, cycle_number);


-- 4. WRITTEN SUBMISSION FILE
-- Supports 3 answer formats: IMAGE, PDF (file_url points to stored file),
-- or TEXT (text_content holds the answer directly, no file involved).
CREATE TABLE written_submission_file (
    id             VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    submission_id  VARCHAR(36)   NOT NULL,
    page_number    INT           NOT NULL DEFAULT 1,
    file_url       VARCHAR(500),
    text_content   TEXT,
    file_type      VARCHAR(10)   NOT NULL DEFAULT 'IMAGE'
                        CHECK (file_type IN ('IMAGE','PDF','TEXT')),
    uploaded_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_submission_file_submission
        FOREIGN KEY (submission_id) REFERENCES written_submission(id),
    CONSTRAINT uk_written_submission_file_page UNIQUE (submission_id, page_number),
    CONSTRAINT chk_written_submission_file_content
        CHECK (
            (file_type = 'TEXT' AND text_content IS NOT NULL) OR
            (file_type IN ('IMAGE','PDF') AND file_url IS NOT NULL)
        )
);

CREATE INDEX idx_written_submission_file_submission ON written_submission_file (submission_id);


-- 5. WRITTEN EVALUATION
CREATE TABLE written_evaluation (
    id                     VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    submission_id          VARCHAR(36)   NOT NULL,
    evaluation_mode         VARCHAR(10)   NOT NULL DEFAULT 'MANUAL'
                            CHECK (evaluation_mode IN ('MANUAL','AI','HYBRID')),
    status                 VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING','PROCESSING','PENDING_REVIEW','COMPLETED','FAILED')),
    total_mark             DECIMAL(6,2),
    evaluated_by_admin_id   VARCHAR(36),
    ai_raw_response         TEXT,
    evaluated_at            TIMESTAMP,
    created_at              TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_evaluation_submission
        FOREIGN KEY (submission_id) REFERENCES written_submission(id),
    CONSTRAINT fk_written_evaluation_admin
        FOREIGN KEY (evaluated_by_admin_id) REFERENCES admin_users(id),
    CONSTRAINT uk_written_evaluation_submission UNIQUE (submission_id)
);

CREATE INDEX idx_written_evaluation_submission ON written_evaluation (submission_id);
CREATE INDEX idx_written_evaluation_status     ON written_evaluation (status);


-- 6. WRITTEN EVALUATION DETAIL
-- obtained_mark / max_mark = final confirmed marks (manual flow, or admin-confirmed AI flow).
-- predicted_mark_manual / predicted_mark_ai + match_score_* = AI-mode suggestions shown to
-- admin (computed via code-based text matching against manual vs AI reference answers),
-- left for admin to review and finalize into obtained_mark.
CREATE TABLE written_evaluation_detail (
    id                     VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    evaluation_id          VARCHAR(36)   NOT NULL,
    question_id            VARCHAR(36)   NOT NULL,
    part                   VARCHAR(1)    NOT NULL CHECK (part IN ('A','B','C','D')),
    obtained_mark          DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    max_mark               DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    predicted_mark_manual  DECIMAL(5,2),
    predicted_mark_ai      DECIMAL(5,2),
    match_score_manual     DECIMAL(5,4),
    match_score_ai         DECIMAL(5,4),
    feedback               TEXT,
    created_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_eval_detail_evaluation
        FOREIGN KEY (evaluation_id) REFERENCES written_evaluation(id),
    CONSTRAINT fk_written_eval_detail_question
        FOREIGN KEY (question_id) REFERENCES written_question(id),
    CONSTRAINT uk_written_eval_detail_part UNIQUE (evaluation_id, question_id, part)
);

CREATE INDEX idx_written_eval_detail_evaluation ON written_evaluation_detail (evaluation_id);
CREATE INDEX idx_written_eval_detail_question   ON written_evaluation_detail (question_id);


-- 7. WRITTEN SUBMISSION TRANSCRIPT
-- Caches the OCR/transcribed text of a student's handwritten (IMAGE/PDF) answer per
-- question+part, so re-evaluation doesn't re-trigger an AI transcription call.
-- Not needed when the submission file_type is TEXT (text_content is used directly).
CREATE TABLE written_submission_transcript (
    id                VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    submission_id     VARCHAR(36)   NOT NULL,
    question_id       VARCHAR(36)   NOT NULL,
    part              VARCHAR(1)    NOT NULL CHECK (part IN ('A','B','C','D')),
    transcribed_text  TEXT,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_transcript_submission
        FOREIGN KEY (submission_id) REFERENCES written_submission(id),
    CONSTRAINT fk_transcript_question
        FOREIGN KEY (question_id) REFERENCES written_question(id),
    CONSTRAINT uk_transcript_part UNIQUE (submission_id, question_id, part)
);

CREATE INDEX idx_transcript_submission ON written_submission_transcript (submission_id);


-- 8. WRITTEN PRACTICE
CREATE TABLE written_practice (
    id                VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    original_exam_id  VARCHAR(36)   NOT NULL,
    archived_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_practice_exam
        FOREIGN KEY (original_exam_id) REFERENCES written_exam(id),
    CONSTRAINT uk_written_practice_exam UNIQUE (original_exam_id)
);

CREATE INDEX idx_written_practice_exam ON written_practice (original_exam_id);


-- 9. WRITTEN SETTINGS (singleton)
CREATE TABLE written_settings (
    id                        VARCHAR(36)   NOT NULL,
    default_evaluation_mode   VARCHAR(10)   NOT NULL DEFAULT 'MANUAL'
                                CHECK (default_evaluation_mode IN ('MANUAL','AI','HYBRID')),
    allowed_submission_types  VARCHAR(100)  NOT NULL DEFAULT 'CAMERA,GALLERY,PDF',
    result_publish_mode       VARCHAR(10)   NOT NULL DEFAULT 'MANUAL'
                                CHECK (result_publish_mode IN ('INSTANT','MANUAL')),
    practice_archive_mode     VARCHAR(10)   NOT NULL DEFAULT 'AUTO'
                                CHECK (practice_archive_mode IN ('AUTO','DISABLE')),
    updated_by_admin_id       VARCHAR(36),
    created_at                TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_settings_admin
        FOREIGN KEY (updated_by_admin_id) REFERENCES admin_users(id)
);

INSERT INTO written_settings (id, default_evaluation_mode, allowed_submission_types,
    result_publish_mode, practice_archive_mode)
VALUES ('default', 'MANUAL', 'CAMERA,GALLERY,PDF', 'MANUAL', 'AUTO');


-- 10. WRITTEN EXAM ATTEMPT HISTORY
CREATE TABLE written_exam_attempt_history (
    id                 VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    exam_id            VARCHAR(36)   NOT NULL,
    user_id            VARCHAR(36)   NOT NULL,
    submission_id      VARCHAR(36)   NOT NULL,
    cycle_number       INT           NOT NULL DEFAULT 1,
    attempt_number     INT           NOT NULL DEFAULT 1,
    is_practice_mode   BOOLEAN       NOT NULL DEFAULT FALSE,
    status             VARCHAR(20)   NOT NULL
                            CHECK (status IN ('SUBMITTED','UNDER_REVIEW','COMPLETED')),
    started_at         TIMESTAMP     NOT NULL,
    submitted_at       TIMESTAMP     NOT NULL,
    time_taken_seconds INT           NOT NULL DEFAULT 0,
    total_questions    INT           NOT NULL DEFAULT 0,
    total_marks        DECIMAL(6,2)  NOT NULL DEFAULT 0.00,
    obtained_marks     DECIMAL(6,2)  NOT NULL DEFAULT 0.00,
    evaluation_mode    VARCHAR(10)   NOT NULL DEFAULT 'MANUAL'
                            CHECK (evaluation_mode IN ('MANUAL','AI','HYBRID')),
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_attempt_history_exam
        FOREIGN KEY (exam_id) REFERENCES written_exam(id),
    CONSTRAINT fk_written_attempt_history_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_written_attempt_history_submission
        FOREIGN KEY (submission_id) REFERENCES written_submission(id)
);

CREATE INDEX idx_written_attempt_history_exam  ON written_exam_attempt_history (exam_id);
CREATE INDEX idx_written_attempt_history_user  ON written_exam_attempt_history (user_id);
CREATE INDEX idx_written_attempt_history_cycle ON written_exam_attempt_history (exam_id, user_id, cycle_number);


-- 11. updated_at auto triggers
DROP TRIGGER IF EXISTS trg_written_exam_updated_at ON written_exam;
CREATE TRIGGER trg_written_exam_updated_at
BEFORE UPDATE ON written_exam
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_written_question_updated_at ON written_question;
CREATE TRIGGER trg_written_question_updated_at
BEFORE UPDATE ON written_question
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_written_submission_updated_at ON written_submission;
CREATE TRIGGER trg_written_submission_updated_at
BEFORE UPDATE ON written_submission
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_written_evaluation_updated_at ON written_evaluation;
CREATE TRIGGER trg_written_evaluation_updated_at
BEFORE UPDATE ON written_evaluation
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_written_settings_updated_at ON written_settings;
CREATE TRIGGER trg_written_settings_updated_at
BEFORE UPDATE ON written_settings
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
