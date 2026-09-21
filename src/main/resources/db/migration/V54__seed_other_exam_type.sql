-- V54__seed_other_exam_type.sql
-- Misc/OTHER exam category টা exam_types টেবিলে যোগ করা হলো
-- (target-level dynamic করার জন্য, যাতে সব category ঐ টেবিল থেকেই আসে)

INSERT INTO exam_types (
    id,
    name,
    name_bn,
    code,
    conducting_body
)
VALUES (
    gen_random_uuid()::text,
    'Other',
    'অন্যান্য',
    'OTHER',
    NULL
)
ON CONFLICT (code) DO NOTHING;
