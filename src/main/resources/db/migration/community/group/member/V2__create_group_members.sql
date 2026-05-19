CREATE TABLE group_members (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    kicked_at TIMESTAMP,
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT uk_group_members_group_user UNIQUE (group_id, user_id)
);

CREATE INDEX idx_group_members_group_status ON group_members(group_id, status);
CREATE INDEX idx_group_members_user_status ON group_members(user_id, status);
