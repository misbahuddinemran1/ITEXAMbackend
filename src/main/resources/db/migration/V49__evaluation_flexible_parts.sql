-- V49: Evaluation module-কে HSC-স্টাইল fixed 4-part (A/B/C/D) থেকে flexible sub-question
-- parts-এ আনা হলো (BCS Written স্টাইল), Phase-1 (written_question) এর ধারাবাহিকতায়।

ALTER TABLE written_exam DROP COLUMN IF EXISTS part_a_mode;
ALTER TABLE written_exam DROP COLUMN IF EXISTS part_b_mode;
ALTER TABLE written_exam DROP COLUMN IF EXISTS part_c_mode;
ALTER TABLE written_exam DROP COLUMN IF EXISTS part_d_mode;

CREATE TABLE written_exam_ai_part_order (
    exam_id    VARCHAR(36) NOT NULL REFERENCES written_exam(id) ON DELETE CASCADE,
    part_order INTEGER     NOT NULL
);
CREATE INDEX idx_written_exam_ai_part_order_exam ON written_exam_ai_part_order (exam_id);

ALTER TABLE written_evaluation_detail ADD COLUMN part_order INTEGER;
UPDATE written_evaluation_detail SET part_order = CASE part
    WHEN 'A' THEN 1 WHEN 'B' THEN 2 WHEN 'C' THEN 3 WHEN 'D' THEN 4 END;
ALTER TABLE written_evaluation_detail ALTER COLUMN part_order SET NOT NULL;

ALTER TABLE written_evaluation_detail DROP CONSTRAINT IF EXISTS uk_written_eval_detail_part;
ALTER TABLE written_evaluation_detail DROP COLUMN part;
ALTER TABLE written_evaluation_detail
    ADD CONSTRAINT uk_written_eval_detail_part UNIQUE (evaluation_id, question_id, part_order);

ALTER TABLE written_submission_transcript ADD COLUMN part_order INTEGER;
UPDATE written_submission_transcript SET part_order = CASE part
    WHEN 'A' THEN 1 WHEN 'B' THEN 2 WHEN 'C' THEN 3 WHEN 'D' THEN 4 END;
ALTER TABLE written_submission_transcript ALTER COLUMN part_order SET NOT NULL;

ALTER TABLE written_submission_transcript DROP CONSTRAINT IF EXISTS uk_transcript_part;
ALTER TABLE written_submission_transcript DROP COLUMN part;
ALTER TABLE written_submission_transcript
    ADD CONSTRAINT uk_transcript_part UNIQUE (submission_id, question_id, part_order);
