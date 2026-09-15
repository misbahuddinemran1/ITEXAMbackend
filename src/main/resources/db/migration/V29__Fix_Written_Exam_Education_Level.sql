-- V30__Fix_Written_Exam_Education_Level.sql

-- ১) পুরনো constraint সরাও
ALTER TABLE written_exam DROP CONSTRAINT IF EXISTS written_exam_education_level_check;

-- ২) কলামের সাইজ বড় করো
ALTER TABLE written_exam ALTER COLUMN education_level TYPE VARCHAR(20);

-- ৩) পুরনো/অচেনা ভ্যালুগুলো নতুন enum এর সাথে ম্যাপ করো
UPDATE written_exam SET education_level = 'HSC_1ST_YEAR' WHERE education_level = 'HSC';
UPDATE written_exam SET education_level = 'HSC_1ST_YEAR' WHERE education_level = 'HONORS';
UPDATE written_exam SET education_level = 'HSC_2ND_YEAR' WHERE education_level = 'MASTERS';
UPDATE written_exam SET education_level = 'SSC' WHERE education_level = 'OTHER' OR education_level IS NULL;

-- ৪) এখন নতুন constraint বসাও (সব row already valid values এ আছে)
ALTER TABLE written_exam ADD CONSTRAINT written_exam_education_level_check
    CHECK (education_level IN ('CLASS_9','NEW_CLASS_10','SSC','HSC_1ST_YEAR','HSC_2ND_YEAR'));
