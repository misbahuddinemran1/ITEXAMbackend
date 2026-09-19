-- V48: written_question-কে HSC-স্টাইল ফিক্সড ৪-part (A/B/C/D) থেকে flexible parts-এ আনা
-- হলো (BCS Written স্টাইল — প্রতিটা মূল প্রশ্নে admin ইচ্ছামতো ২টা/৩টা/যেকোনো সংখ্যক sub-part
-- দিতে পারবে)। পুরনো part_a/b/c/d কলামগুলো ডাটা-loss এড়াতে রেখে দেওয়া হলো (nullable করে,
-- backward compatibility-র জন্য) — নতুন প্রশ্নগুলো written_question_part টেবিল ব্যবহার করবে।

CREATE TABLE written_question_part (
    id             VARCHAR(36) PRIMARY KEY,
    question_id    VARCHAR(36) NOT NULL REFERENCES written_question(id) ON DELETE CASCADE,
    part_order     INTEGER     NOT NULL,
    question_text  TEXT        NOT NULL,
    model_answer   TEXT,
    ai_answer      TEXT,
    marking_scheme TEXT,
    max_mark       NUMERIC(5,2) NOT NULL,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);

CREATE INDEX idx_written_question_part_question ON written_question_part (question_id);
CREATE INDEX idx_written_question_part_order    ON written_question_part (question_id, part_order);

-- পুরনো fixed A/B/C/D কলাম আর বাধ্যতামূলক না (নতুন প্রশ্নে খালি থাকবে, parts টেবিল ব্যবহার হবে)
ALTER TABLE written_question ALTER COLUMN part_a_question DROP NOT NULL;
ALTER TABLE written_question ALTER COLUMN part_b_question DROP NOT NULL;
ALTER TABLE written_question ALTER COLUMN part_c_question DROP NOT NULL;
ALTER TABLE written_question ALTER COLUMN part_d_question DROP NOT NULL;

ALTER TABLE written_question ALTER COLUMN part_a_max_mark DROP NOT NULL;
ALTER TABLE written_question ALTER COLUMN part_b_max_mark DROP NOT NULL;
ALTER TABLE written_question ALTER COLUMN part_c_max_mark DROP NOT NULL;
ALTER TABLE written_question ALTER COLUMN part_d_max_mark DROP NOT NULL;

ALTER TABLE written_question ALTER COLUMN part_a_max_mark DROP DEFAULT;
ALTER TABLE written_question ALTER COLUMN part_b_max_mark DROP DEFAULT;
ALTER TABLE written_question ALTER COLUMN part_c_max_mark DROP DEFAULT;
ALTER TABLE written_question ALTER COLUMN part_d_max_mark DROP DEFAULT;
