CREATE TABLE user_badges (
    id          UUID        PRIMARY KEY,
    user_id     UUID        NOT NULL,
    badge_code  VARCHAR(50) NOT NULL,
    earned_at   TIMESTAMP   NOT NULL,
    CONSTRAINT fk_user_badges_badge FOREIGN KEY (badge_code) REFERENCES badges(code),
    CONSTRAINT uk_user_badges_user_badge UNIQUE (user_id, badge_code)
);

CREATE INDEX idx_user_badges_user_earned ON user_badges(user_id, earned_at DESC);
