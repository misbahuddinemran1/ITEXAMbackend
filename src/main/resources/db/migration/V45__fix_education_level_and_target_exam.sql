-- V45: revert V44's wrong constraint on education_level (that was meant for job category,
-- but education_level is actually the user's current academic level), and add a proper
-- constraint for target_exam (which IS the job-category field).

-- 1) Fix education_level: drop wrong constraint, reset test data, add correct constraint
ALTER TABLE users
DROP CONSTRAINT IF EXISTS users_education_level_check;

UPDATE users
SET education_level = 'OTHER'
WHERE education_level IS NOT NULL;

ALTER TABLE users
ADD CONSTRAINT users_education_level_check CHECK (education_level IN (
    'HONOURS', 'ENGINEERING', 'DEGREE', 'MASTERS', 'DIPLOMA', 'OTHER'
));

-- 2) Add constraint for target_exam (job category) — this is where BCS/Bank/NTRC/Govt belongs
UPDATE users
SET target_exam = NULL
WHERE target_exam IS NOT NULL
  AND target_exam NOT IN ('BCS_ICT', 'NTRCA_ICT', 'BANK_IT', 'GOVT_IT', 'OTHER');

ALTER TABLE users
ADD CONSTRAINT users_target_exam_check CHECK (target_exam IN (
    'BCS_ICT', 'NTRCA_ICT', 'BANK_IT', 'GOVT_IT', 'OTHER'
));
