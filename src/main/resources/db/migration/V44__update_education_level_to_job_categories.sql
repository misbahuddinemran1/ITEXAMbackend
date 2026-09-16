-- V44: education_level কে job-category ভিত্তিক করা (HSC app থেকে job-exam app-এ পিভট)

ALTER TABLE users
DROP CONSTRAINT IF EXISTS users_education_level_check;

UPDATE users
SET education_level = 'OTHER'
WHERE education_level IN ('CLASS_9', 'NEW_CLASS_10', 'SSC', 'HSC_1ST_YEAR', 'HSC_2ND_YEAR', 'HONORS', 'MASTERS');

ALTER TABLE users
ADD CONSTRAINT users_education_level_check
CHECK (education_level IN (
    'BCS_ICT', 'NTRCA_ICT', 'BANK_IT', 'GOVT_IT', 'OTHER'
));
