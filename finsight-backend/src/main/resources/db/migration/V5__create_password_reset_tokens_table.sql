CREATE TABLE password_reset_tokens
(
    id          UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    token       NVARCHAR(255)    NOT NULL,
    user_id     UNIQUEIDENTIFIER NOT NULL,
    expires_at  DATETIMEOFFSET(7) NOT NULL,
    created_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by  NVARCHAR(255)    DEFAULT N'SYSTEM',
    updated_by  NVARCHAR(255)    DEFAULT N'SYSTEM',

    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT uk_password_reset_tokens_user_id UNIQUE (user_id),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_password_reset_tokens_expires_at
    ON password_reset_tokens (expires_at);
