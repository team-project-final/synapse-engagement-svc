CREATE TABLE user_profiles_gamification (
    user_id UUID PRIMARY KEY,
    total_xp INTEGER NOT NULL,
    level INTEGER NOT NULL,
    current_streak INTEGER NOT NULL,
    longest_streak INTEGER NOT NULL,
    title VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE xp_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    xp_amount INTEGER NOT NULL,
    source_id VARCHAR(100) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_xp_events_user_profile
        FOREIGN KEY (user_id) REFERENCES user_profiles_gamification(user_id),
    CONSTRAINT uk_xp_events_event_id UNIQUE (event_id),
    CONSTRAINT uk_xp_events_event_source UNIQUE (user_id, event_type, source_id)
);

CREATE INDEX idx_xp_events_user_created ON xp_events(user_id, created_at DESC);
CREATE INDEX idx_xp_events_user_type ON xp_events(user_id, event_type);
