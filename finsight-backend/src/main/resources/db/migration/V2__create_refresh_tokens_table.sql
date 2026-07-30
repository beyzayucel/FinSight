CREATE TABLE refresh_tokens
(
    id          UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    token_hash  NVARCHAR(64)     NOT NULL,
    expiry_date DATETIMEOFFSET(7)     NOT NULL,
    revoked     BIT              NOT NULL DEFAULT 0,
    revoked_at  DATETIMEOFFSET(7),
    user_id     UNIQUEIDENTIFIER NOT NULL,
    created_at  DATETIMEOFFSET(7)     NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at  DATETIMEOFFSET(7)     NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by  NVARCHAR(255)    DEFAULT N'SYSTEM',
    updated_by  NVARCHAR(255)    DEFAULT N'SYSTEM',

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
