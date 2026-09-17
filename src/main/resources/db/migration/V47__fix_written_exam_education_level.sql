-- V47: written_exam.education_level এখনো পুরনো HSC-ভিত্তিক constraint ব্যবহার করছিল
-- (CLASS_9/SSC/HSC_1ST_YEAR/HSC_2ND_YEAR — V29 থেকে), কিন্তু frontend পাঠায় job-category
-- ভ্যালু (BCS_ICT/NTRCA_ICT/BANK_IT/GOVT_IT/OTHER) যেটা users.target_exam-এ V45-এ ঠিক
-- করা হয়েছিল। written_exam-এও একই job-category constraint বসানো হলো।

ALTER TABLE written_exam
DROP CONSTRAINT IF EXISTS written_exam_education_level_check;

UPDATE written_exam
SET education_level = 'OTHER'
WHERE education_level IS NOT NULL
  AND education_level NOT IN ('BCS_ICT', 'NTRCA_ICT', 'BANK_IT', 'GOVT_IT', 'OTHER');

ALTER TABLE written_exam
ADD CONSTRAINT written_exam_education_level_check CHECK (education_level IN (
    'BCS_ICT', 'NTRCA_ICT', 'BANK_IT', 'GOVT_IT', 'OTHER'
));
