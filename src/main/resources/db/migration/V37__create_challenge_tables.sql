-- 1vs1 Challenge feature (Guide Practice MCQ ভিত্তিক)

CREATE TABLE challenge (
    id VARCHAR(36) PRIMARY KEY,
    mode VARCHAR(20) NOT NULL,                      -- FRIEND / RANDOM
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING/ACTIVE/COMPLETED/EXPIRED/DECLINED
    creator_id VARCHAR(36) NOT NULL,
    opponent_id VARCHAR(36),                         -- RANDOM মোডে matched হওয়ার আগে null
    chapter_id VARCHAR(36),
    topic_id VARCHAR(36),                            -- null = chapter-এর সব topic মিশিয়ে
    question_count INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_challenge_creator FOREIGN KEY (creator_id) REFERENCES users(id),
    CONSTRAINT fk_challenge_opponent FOREIGN KEY (opponent_id) REFERENCES users(id),
    CONSTRAINT fk_challenge_chapter FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT fk_challenge_topic FOREIGN KEY (topic_id) REFERENCES topics(id)
);

CREATE INDEX idx_challenge_creator ON challenge(creator_id);
CREATE INDEX idx_challenge_opponent ON challenge(opponent_id);
CREATE INDEX idx_challenge_status ON challenge(status);
CREATE INDEX idx_challenge_mode_status ON challenge(mode, status);

CREATE TABLE challenge_question (
    id VARCHAR(36) PRIMARY KEY,
    challenge_id VARCHAR(36) NOT NULL,
    mcq_id VARCHAR(36) NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_cq_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(id) ON DELETE CASCADE,
    CONSTRAINT fk_cq_mcq FOREIGN KEY (mcq_id) REFERENCES guide_practice_mcq(id)
);

CREATE INDEX idx_challenge_question_challenge ON challenge_question(challenge_id);

CREATE TABLE challenge_attempt (
    id VARCHAR(36) PRIMARY KEY,
    challenge_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    mcq_id VARCHAR(36) NOT NULL,
    selected_option_id VARCHAR(36),
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    time_taken_ms INT,
    answered_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_ca_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(id) ON DELETE CASCADE,
    CONSTRAINT fk_ca_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ca_mcq FOREIGN KEY (mcq_id) REFERENCES guide_practice_mcq(id),
    CONSTRAINT fk_ca_option FOREIGN KEY (selected_option_id) REFERENCES guide_practice_mcq_option(id),
    CONSTRAINT uq_challenge_attempt UNIQUE (challenge_id, user_id, mcq_id)
);

CREATE INDEX idx_challenge_attempt_challenge_user ON challenge_attempt(challenge_id, user_id);

CREATE TABLE challenge_result (
    id VARCHAR(36) PRIMARY KEY,
    challenge_id VARCHAR(36) NOT NULL UNIQUE,
    winner_id VARCHAR(36),                    -- null = draw
    creator_score INT NOT NULL DEFAULT 0,
    opponent_score INT NOT NULL DEFAULT 0,
    creator_points_earned INT NOT NULL DEFAULT 0,
    opponent_points_earned INT NOT NULL DEFAULT 0,
    completed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_cr_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(id) ON DELETE CASCADE,
    CONSTRAINT fk_cr_winner FOREIGN KEY (winner_id) REFERENCES users(id)
);

CREATE TABLE user_challenge_stats (
    user_id VARCHAR(36) PRIMARY KEY,
    total_points INT NOT NULL DEFAULT 0,
    total_wins INT NOT NULL DEFAULT 0,
    total_losses INT NOT NULL DEFAULT 0,
    total_draws INT NOT NULL DEFAULT 0,
    total_played INT NOT NULL DEFAULT 0,
    current_win_streak INT NOT NULL DEFAULT 0,
    best_win_streak INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_ucs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_challenge_stats_points ON user_challenge_stats(total_points DESC);
