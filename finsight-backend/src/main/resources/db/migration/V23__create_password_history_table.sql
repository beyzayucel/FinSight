CREATE TABLE password_history
(
    id            UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    user_id       UNIQUEIDENTIFIER  NOT NULL,
    password_hash NVARCHAR(255)     NOT NULL,
    created_at    DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at    DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by    NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by    NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_password_history PRIMARY KEY (id),
    CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Son N sifreyi en yeniden eskiye dogru okumak icin
CREATE INDEX idx_password_history_user_created
    ON password_history (user_id, created_at DESC);
