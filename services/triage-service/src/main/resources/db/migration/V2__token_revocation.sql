CREATE TABLE revoked_tokens (
    id UUID PRIMARY KEY,
    token_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_revoked_tokens_token_id ON revoked_tokens (token_id);
CREATE INDEX idx_revoked_tokens_expires_at ON revoked_tokens (expires_at);
