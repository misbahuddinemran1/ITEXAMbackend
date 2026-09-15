ALTER TABLE written_exam ADD COLUMN IF NOT EXISTS practice_enabled BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE written_exam ADD COLUMN IF NOT EXISTS show_result_in_practice BOOLEAN NOT NULL DEFAULT false;
