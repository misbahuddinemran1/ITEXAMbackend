ALTER TABLE written_question ADD COLUMN is_board_question BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE written_question ADD COLUMN board VARCHAR(50);
ALTER TABLE written_question ADD COLUMN exam_year INT;
