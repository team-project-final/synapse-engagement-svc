-- 그룹 전용 공유 콘텐츠(FE-05): group_id가 NULL이면 기존과 동일한 공개 공유다.
ALTER TABLE shared_contents ADD COLUMN group_id BIGINT;

ALTER TABLE shared_contents ADD CONSTRAINT fk_shared_contents_group
    FOREIGN KEY (group_id) REFERENCES groups(id);

CREATE INDEX idx_shared_contents_group_id ON shared_contents(group_id);
