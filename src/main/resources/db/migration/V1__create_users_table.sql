CREATE TABLE users
(
    id              UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    email           NVARCHAR(320)    NOT NULL,
    username        NVARCHAR(100)    NOT NULL,
    password        NVARCHAR(255)    NOT NULL,
    first_name      NVARCHAR(50)     NOT NULL,
    last_name       NVARCHAR(50)     NOT NULL,
    phone_number    NVARCHAR(16)     NOT NULL,
    email_verified  BIT              NOT NULL DEFAULT 0,
    enabled         BIT              NOT NULL DEFAULT 1,
    first_login     BIT              NOT NULL DEFAULT 1,
    role            NVARCHAR(20)     NOT NULL DEFAULT N'USER',
    last_login_at   DATETIMEOFFSET(7),
    deleted         BIT              NOT NULL DEFAULT 0,
    deleted_at      DATETIMEOFFSET(7),
    created_at      DATETIMEOFFSET(7)     NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at      DATETIMEOFFSET(7)     NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by      NVARCHAR(255)    DEFAULT N'SYSTEM',
    updated_by      NVARCHAR(255)    DEFAULT N'SYSTEM',

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);

-- Filtered unique index: allows multiple NULLs, enforces uniqueness on non-null values
CREATE UNIQUE INDEX ix_users_phone_number ON users (phone_number) WHERE phone_number IS NOT NULL;
