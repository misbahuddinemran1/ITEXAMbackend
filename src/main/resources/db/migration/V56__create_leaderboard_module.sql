-- =========================================
-- V56: Leaderboard Module
-- Live Exam এর Overall + Monthly leaderboard
-- education_level কলামে এই প্রজেক্টে ছাত্রের Target Exam (Exam Category এর code) জমা থাকে,
-- তাই দৈর্ঘ্য ৩০ (exam_types.code এর সমান)।
-- =========================================

-- 1. LEADERBOARD SETTINGS (একটাই row, Admin নিয়ন্ত্রণ করে)
CREATE TABLE IF NOT EXISTS leaderboard_settings (
    id                           VARCHAR(36)  NOT NULL,
    overall_min_exams_required   INT          NOT NULL DEFAULT 5,
    monthly_threshold_type       VARCHAR(20)  NOT NULL DEFAULT 'RELATIVE'
                                     CHECK (monthly_threshold_type IN ('FIXED','RELATIVE')),
    monthly_min_exams_required   INT          NOT NULL DEFAULT 5,
    monthly_allowed_missed_exams INT          NOT NULL DEFAULT 2,
    level_wise_separate          BOOLEAN      NOT NULL DEFAULT TRUE,
    is_enabled                   BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_by_admin_id          VARCHAR(36),
    created_at                   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

INSERT INTO leaderboard_settings (id, overall_min_exams_required, monthly_threshold_type,
    monthly_min_exams_required, monthly_allowed_missed_exams, level_wise_separate, is_enabled)
VALUES ('default', 5, 'RELATIVE', 5, 2, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;


-- 2. OVERALL STATS (cache, প্রতি ছাত্রের সর্বকালের হিসাব)
CREATE TABLE IF NOT EXISTS user_leaderboard_stats (
    id                 VARCHAR(36)   NOT NULL,
    user_id            VARCHAR(36)   NOT NULL,
    education_level    VARCHAR(30),
    total_exams_taken  INT           NOT NULL DEFAULT 0,
    total_points       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    avg_score_percent  DECIMAL(6,2)  NOT NULL DEFAULT 0.00,
    last_updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_leaderboard_stats_user UNIQUE (user_id)
);
CREATE INDEX IF NOT EXISTS idx_uls_level_points
    ON user_leaderboard_stats(education_level, total_points DESC);


-- 3. MONTHLY STATS (cache, প্রতি ছাত্রের প্রতি মাসের হিসাব)
CREATE TABLE IF NOT EXISTS user_monthly_leaderboard_stats (
    id                           VARCHAR(36)   NOT NULL,
    user_id                      VARCHAR(36)   NOT NULL,
    education_level              VARCHAR(30),
    year_month                   VARCHAR(7)    NOT NULL,
    exams_taken_this_month       INT           NOT NULL DEFAULT 0,
    total_points_this_month      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    avg_score_percent_this_month DECIMAL(6,2)  NOT NULL DEFAULT 0.00,
    last_updated_at              TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at                   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_monthly_leaderboard_stats UNIQUE (user_id, year_month)
);
CREATE INDEX IF NOT EXISTS idx_umls_month_level_points
    ON user_monthly_leaderboard_stats(year_month, education_level, total_points_this_month DESC);


-- 4. updated_at auto trigger (V18 এ তৈরি set_updated_at() function ব্যবহার করে)
DROP TRIGGER IF EXISTS trg_leaderboard_settings_updated_at ON leaderboard_settings;
CREATE TRIGGER trg_leaderboard_settings_updated_at
BEFORE UPDATE ON leaderboard_settings
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
