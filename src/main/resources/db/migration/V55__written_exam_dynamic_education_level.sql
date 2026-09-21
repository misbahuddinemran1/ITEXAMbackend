-- V55: Written Exam-এর education_level এখন Exam Category (exam_types.code) থেকে dynamic আসে।
-- V47-এর হার্ডকোড ৫-মানের CHECK constraint সরানো হলো, যাতে Super Admin-এর যোগ করা নতুন
-- category-র code দিয়েও Written Exam সেভ করা যায়। exam_types.code VARCHAR(30) তাই এখানেও ৩০।
ALTER TABLE written_exam
DROP CONSTRAINT IF EXISTS written_exam_education_level_check;

ALTER TABLE written_exam
ALTER COLUMN education_level TYPE VARCHAR(30);
