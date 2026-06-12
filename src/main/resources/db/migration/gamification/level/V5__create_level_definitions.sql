CREATE TABLE level_definitions (
    id          UUID         PRIMARY KEY,
    level_number INTEGER     NOT NULL UNIQUE,
    min_xp      INTEGER     NOT NULL UNIQUE,
    title       VARCHAR(50) NOT NULL
);

INSERT INTO level_definitions (id, level_number, min_xp, title) VALUES
    (gen_random_uuid(), 1,    0,    'Novice'),
    (gen_random_uuid(), 2,    100,  'Apprentice'),
    (gen_random_uuid(), 3,    300,  'Learner'),
    (gen_random_uuid(), 4,    600,  'Student'),
    (gen_random_uuid(), 5,    1000, 'Scholar'),
    (gen_random_uuid(), 6,    1500, 'Expert'),
    (gen_random_uuid(), 7,    2100, 'Master'),
    (gen_random_uuid(), 8,    2800, 'Sage'),
    (gen_random_uuid(), 9,    3600, 'Legend'),
    (gen_random_uuid(), 10,   4500, 'Grandmaster');
