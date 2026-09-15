-- ChallengeQuestion, ChallengeAttempt, ChallengeResult সব BaseEntity extend করে
-- (created_at + updated_at দুটোই লাগে), কিন্তু V58 migration এ এগুলো বাদ পড়ে গিয়েছিল

ALTER TABLE challenge_question ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE challenge_question ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE challenge_attempt ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE challenge_attempt ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE challenge_result ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE challenge_result ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();
