-- V60__fix_question_concepts_updated_at.sql
-- QuestionConcept entity (BaseEntity) expects updated_at, but V7 migration
-- created question_concepts without it. Add it with a safe default/backfill.

ALTER TABLE question_concepts
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE question_concepts SET updated_at = created_at WHERE updated_at IS NULL;
