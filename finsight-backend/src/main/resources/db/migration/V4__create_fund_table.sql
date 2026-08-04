CREATE TABLE funds
(
    id          UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    code        NVARCHAR(3)       NOT NULL,
    [date]      DATE              NOT NULL,
    created_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by  NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by  NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_funds PRIMARY KEY (id),
    CONSTRAINT uk_funds_code UNIQUE (code)
);