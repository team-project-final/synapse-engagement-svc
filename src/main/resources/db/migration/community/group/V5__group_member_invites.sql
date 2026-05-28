ALTER TABLE group_members
    ADD COLUMN invite_token VARCHAR(80),
    ADD COLUMN invite_expires_at TIMESTAMP;

CREATE UNIQUE INDEX uk_group_members_invite_token
    ON group_members(invite_token)
    WHERE invite_token IS NOT NULL;

CREATE INDEX idx_group_members_group_status
    ON group_members(group_id, status);
