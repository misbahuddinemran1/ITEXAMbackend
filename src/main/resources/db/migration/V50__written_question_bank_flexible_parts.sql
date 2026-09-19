CREATE TABLE written_question_bank_part (
    id               VARCHAR(36) PRIMARY KEY,
    bank_question_id VARCHAR(36) NOT NULL REFERENCES written_question_bank(id) ON DELETE CASCADE,
    part_order       INTEGER     NOT NULL,
    question_text    TEXT        NOT NULL,
    model_answer     TEXT,
    ai_answer        TEXT,
    marking_scheme   TEXT,
    max_mark         NUMERIC(5,2) NOT NULL,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE INDEX idx_wqb_part_bank  ON written_question_bank_part (bank_question_id);
CREATE INDEX idx_wqb_part_order ON written_question_bank_part (bank_question_id, part_order);

INSERT INTO written_question_bank_part (id, bank_question_id, part_order, question_text, model_answer, ai_answer, marking_scheme, max_mark, created_at, updated_at)
SELECT gen_random_uuid()::text, id, 1, part_a_question, part_a_model_answer, part_a_ai_answer, part_a_marking_scheme, part_a_max_mark, now(), now() FROM written_question_bank;
INSERT INTO written_question_bank_part (id, bank_question_id, part_order, question_text, model_answer, ai_answer, marking_scheme, max_mark, created_at, updated_at)
SELECT gen_random_uuid()::text, id, 2, part_b_question, part_b_model_answer, part_b_ai_answer, part_b_marking_scheme, part_b_max_mark, now(), now() FROM written_question_bank;
INSERT INTO written_question_bank_part (id, bank_question_id, part_order, question_text, model_answer, ai_answer, marking_scheme, max_mark, created_at, updated_at)
SELECT gen_random_uuid()::text, id, 3, part_c_question, part_c_model_answer, part_c_ai_answer, part_c_marking_scheme, part_c_max_mark, now(), now() FROM written_question_bank;
INSERT INTO written_question_bank_part (id, bank_question_id, part_order, question_text, model_answer, ai_answer, marking_scheme, max_mark, created_at, updated_at)
SELECT gen_random_uuid()::text, id, 4, part_d_question, part_d_model_answer, part_d_ai_answer, part_d_marking_scheme, part_d_max_mark, now(), now() FROM written_question_bank;

ALTER TABLE written_question_bank
    DROP COLUMN part_a_question, DROP COLUMN part_a_model_answer, DROP COLUMN part_a_ai_answer, DROP COLUMN part_a_marking_scheme, DROP COLUMN part_a_max_mark,
    DROP COLUMN part_b_question, DROP COLUMN part_b_model_answer, DROP COLUMN part_b_ai_answer, DROP COLUMN part_b_marking_scheme, DROP COLUMN part_b_max_mark,
    DROP COLUMN part_c_question, DROP COLUMN part_c_model_answer, DROP COLUMN part_c_ai_answer, DROP COLUMN part_c_marking_scheme, DROP COLUMN part_c_max_mark,
    DROP COLUMN part_d_question, DROP COLUMN part_d_model_answer, DROP COLUMN part_d_ai_answer, DROP COLUMN part_d_marking_scheme, DROP COLUMN part_d_max_mark;
