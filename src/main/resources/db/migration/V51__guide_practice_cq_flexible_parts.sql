CREATE TABLE guide_practice_cq_part (
    id              VARCHAR(36) PRIMARY KEY,
    cq_id           VARCHAR(36) NOT NULL,
    part_order      INT NOT NULL,
    question_text   TEXT,
    model_answer    TEXT,
    marking_scheme  TEXT,
    max_mark        INT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_guide_practice_cq_part_cq
        FOREIGN KEY (cq_id) REFERENCES guide_practice_cq(id) ON DELETE CASCADE
);

CREATE INDEX idx_guide_practice_cq_part_cq ON guide_practice_cq_part (cq_id, part_order);

INSERT INTO guide_practice_cq_part (id, cq_id, part_order, question_text, model_answer, marking_scheme, max_mark)
SELECT gen_random_uuid()::text, id, 1, part_a_question, part_a_model_answer, part_a_marking_scheme, part_a_max_mark
FROM guide_practice_cq WHERE part_a_question IS NOT NULL AND part_a_question <> '';
INSERT INTO guide_practice_cq_part (id, cq_id, part_order, question_text, model_answer, marking_scheme, max_mark)
SELECT gen_random_uuid()::text, id, 2, part_b_question, part_b_model_answer, part_b_marking_scheme, part_b_max_mark
FROM guide_practice_cq WHERE part_b_question IS NOT NULL AND part_b_question <> '';
INSERT INTO guide_practice_cq_part (id, cq_id, part_order, question_text, model_answer, marking_scheme, max_mark)
SELECT gen_random_uuid()::text, id, 3, part_c_question, part_c_model_answer, part_c_marking_scheme, part_c_max_mark
FROM guide_practice_cq WHERE part_c_question IS NOT NULL AND part_c_question <> '';
INSERT INTO guide_practice_cq_part (id, cq_id, part_order, question_text, model_answer, marking_scheme, max_mark)
SELECT gen_random_uuid()::text, id, 4, part_d_question, part_d_model_answer, part_d_marking_scheme, part_d_max_mark
FROM guide_practice_cq WHERE part_d_question IS NOT NULL AND part_d_question <> '';

-- ফাঁকা part বাদ গেলে order আবার 1,2,3... করে দেওয়া
UPDATE guide_practice_cq_part p SET part_order = r.rn
FROM (SELECT id, ROW_NUMBER() OVER (PARTITION BY cq_id ORDER BY part_order) AS rn FROM guide_practice_cq_part) r
WHERE p.id = r.id;

ALTER TABLE guide_practice_cq
    DROP COLUMN part_a_question, DROP COLUMN part_a_model_answer, DROP COLUMN part_a_marking_scheme, DROP COLUMN part_a_max_mark,
    DROP COLUMN part_b_question, DROP COLUMN part_b_model_answer, DROP COLUMN part_b_marking_scheme, DROP COLUMN part_b_max_mark,
    DROP COLUMN part_c_question, DROP COLUMN part_c_model_answer, DROP COLUMN part_c_marking_scheme, DROP COLUMN part_c_max_mark,
    DROP COLUMN part_d_question, DROP COLUMN part_d_model_answer, DROP COLUMN part_d_marking_scheme, DROP COLUMN part_d_max_mark;
