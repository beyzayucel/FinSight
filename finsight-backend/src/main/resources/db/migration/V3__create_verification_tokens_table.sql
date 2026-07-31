CREATE TABLE verification_tokens
(
    id          UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    token       NVARCHAR(255)    NOT NULL,
    user_id     UNIQUEIDENTIFIER NOT NULL,
    expires_at  DATETIMEOFFSET(7) NOT NULL,
    created_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by  NVARCHAR(255)    DEFAULT N'SYSTEM',
    updated_by  NVARCHAR(255)    DEFAULT N'SYSTEM',

    CONSTRAINT pk_verification_tokens PRIMARY KEY (id),
    CONSTRAINT uk_verification_tokens_token UNIQUE (token),
    CONSTRAINT uk_verification_tokens_user_id UNIQUE (user_id),
    CONSTRAINT fk_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_verification_tokens_expires_at
    ON verification_tokens (expires_at);