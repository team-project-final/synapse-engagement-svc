-- 사용자별 현재 게이미피케이션 상태를 저장합니다.
-- 화면에서 프로필을 조회할 때 매번 xp_events를 합산하지 않도록 누적 XP/레벨을 별도 보관합니다.
CREATE TABLE user_profiles_gamification (
    user_id UUID PRIMARY KEY,
    total_xp INTEGER NOT NULL,
    level INTEGER NOT NULL,
    current_streak INTEGER NOT NULL,
    longest_streak INTEGER NOT NULL,
    title VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- XP 적립 이력 테이블입니다.
-- 한 row는 "어떤 사용자에게 어떤 원본 활동으로 몇 XP가 지급됐는지"를 뜻합니다.
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
    -- 같은 이벤트 메시지가 재전송되어도 XP가 두 번 쌓이지 않도록 막습니다.
    CONSTRAINT uk_xp_events_event_id UNIQUE (event_id),
    -- event_id가 달라도 같은 사용자의 같은 원본 활동은 한 번만 XP를 받을 수 있게 막습니다.
    CONSTRAINT uk_xp_events_event_source UNIQUE (user_id, event_type, source_id)
);

-- 사용자별 최신 XP 이력을 빠르게 조회하기 위한 인덱스입니다.
CREATE INDEX idx_xp_events_user_created ON xp_events(user_id, created_at DESC);
-- 사용자별/활동 종류별 XP 이벤트 분석이나 조회를 대비한 인덱스입니다.
CREATE INDEX idx_xp_events_user_type ON xp_events(user_id, event_type);
