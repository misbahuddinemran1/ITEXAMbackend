-- V57__fix_guide_practice_mcq_option_columns.sql
-- Fix: guide_practice_mcq_option was missing created_at/updated_at columns
-- required by BaseEntity, causing insert failures.

ALTER TABLE guide_practice_mcq_option
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE guide_practice_mcq_option
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();
