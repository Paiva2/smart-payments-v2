CREATE TABLE authentication_black_list (
    id BIGSERIAL PRIMARY KEY,
    token_hash CHAR(100) NOT NULL UNIQUE,
    revoked_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
