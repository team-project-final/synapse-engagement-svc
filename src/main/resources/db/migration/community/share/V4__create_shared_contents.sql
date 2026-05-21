CREATE TABLE shared_contents (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    content_type VARCHAR(30) NOT NULL,
    content_id UUID NOT NULL,
    share_token VARCHAR(64) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    tags VARCHAR(500),
    download_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_shared_contents_share_token UNIQUE (share_token)
);

CREATE INDEX idx_shared_contents_owner ON shared_contents(owner_id);
CREATE INDEX idx_shared_contents_type ON shared_contents(content_type);
CREATE INDEX idx_shared_contents_created ON shared_contents(created_at DESC);
CREATE INDEX idx_shared_contents_search ON shared_contents(title, description, tags);
