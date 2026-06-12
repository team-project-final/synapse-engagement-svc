CREATE TABLE badges (
    id            UUID         PRIMARY KEY,
    code          VARCHAR(50)  NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    category      VARCHAR(50),
    criteria_json VARCHAR(500) NOT NULL
);

CREATE INDEX idx_badges_category ON badges(category);

INSERT INTO badges (id, code, name, description, category, criteria_json) VALUES
    (gen_random_uuid(), 'FIRST_STEP',  '첫 걸음',       '첫 XP를 획득했습니다.',          'xp',     '{"type":"xp_threshold","value":1}'),
    (gen_random_uuid(), 'CENTURION',   '백전백승',       'XP 100을 달성했습니다.',          'xp',     '{"type":"xp_threshold","value":100}'),
    (gen_random_uuid(), 'SCHOLAR',     '학자',           'XP 1000을 달성했습니다.',         'xp',     '{"type":"xp_threshold","value":1000}'),
    (gen_random_uuid(), 'LEGEND',      '전설',           'XP 5000을 달성했습니다.',         'xp',     '{"type":"xp_threshold","value":5000}'),
    (gen_random_uuid(), 'STREAK_3',    '3일 연속',       '3일 연속 학습을 달성했습니다.',    'streak', '{"type":"streak_threshold","value":3}'),
    (gen_random_uuid(), 'STREAK_7',    '일주일 학습왕',  '7일 연속 학습을 달성했습니다.',    'streak', '{"type":"streak_threshold","value":7}'),
    (gen_random_uuid(), 'STREAK_30',   '한달 학습왕',    '30일 연속 학습을 달성했습니다.',   'streak', '{"type":"streak_threshold","value":30}'),
    (gen_random_uuid(), 'LEVEL_5',     '중급자',         '레벨 5에 도달했습니다.',          'level',  '{"type":"level_threshold","value":5}'),
    (gen_random_uuid(), 'LEVEL_10',    '마스터',         '레벨 10에 도달했습니다.',         'level',  '{"type":"level_threshold","value":10}');
