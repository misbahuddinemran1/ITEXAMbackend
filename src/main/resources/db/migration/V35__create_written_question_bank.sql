CREATE TABLE written_question_bank (
    id VARCHAR(36) PRIMARY KEY,

    subject_id VARCHAR(36) NOT NULL,
    chapter_id VARCHAR(36) NOT NULL,
    topic_id VARCHAR(36),

    stimulus TEXT NOT NULL,
    stimulus_bn TEXT,

    is_board_question BOOLEAN NOT NULL DEFAULT FALSE,
    board VARCHAR(50),
    exam_year INT,

    part_a_question TEXT NOT NULL,
    part_a_model_answer TEXT,
    part_a_ai_answer TEXT,
    part_a_marking_scheme TEXT,
    part_a_max_mark DECIMAL(5,2) NOT NULL DEFAULT 1.00,

    part_b_question TEXT NOT NULL,
    part_b_model_answer TEXT,
    part_b_ai_answer TEXT,
    part_b_marking_scheme TEXT,
    part_b_max_mark DECIMAL(5,2) NOT NULL DEFAULT 2.00,

    part_c_question TEXT NOT NULL,
    part_c_model_answer TEXT,
    part_c_ai_answer TEXT,
    part_c_marking_scheme TEXT,
    part_c_max_mark DECIMAL(5,2) NOT NULL DEFAULT 3.00,

    part_d_question TEXT NOT NULL,
    part_d_model_answer TEXT,
    part_d_ai_answer TEXT,
    part_d_marking_scheme TEXT,
    part_d_max_mark DECIMAL(5,2) NOT NULL DEFAULT 4.00,

    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_wqb_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_wqb_chapter FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT fk_wqb_topic FOREIGN KEY (topic_id) REFERENCES topics(id)
);
