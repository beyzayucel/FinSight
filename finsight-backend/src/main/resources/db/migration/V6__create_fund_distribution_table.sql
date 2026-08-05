CREATE TABLE fund_distribution
(
    id          UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    fund_id     UNIQUEIDENTIFIER  NOT NULL,
    category    NVARCHAR(255)     NOT NULL,
    weight      DECIMAL(9, 6)     NOT NULL,
    [date]      DATE              NOT NULL,
    created_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by  NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by  NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_fund_distribution PRIMARY KEY (id),
    CONSTRAINT fk_fund_distribution_fund FOREIGN KEY (fund_id) REFERENCES funds (id)
);