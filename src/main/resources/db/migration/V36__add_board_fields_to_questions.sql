ALTER TABLE questions ADD COLUMN is_board_question BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE questions ADD COLUMN board VARCHAR(50);

CREATE INDEX idx_q_board ON questions (board, year_appeared);
