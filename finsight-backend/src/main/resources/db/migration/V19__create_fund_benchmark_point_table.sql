CREATE TABLE fund_benchmark_point
(
    id               UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    fund_id          UNIQUEIDENTIFIER  NOT NULL,
    data_date        DATE              NOT NULL,
    fund_return      DECIMAL(12, 6)    NOT NULL,
    benchmark_return DECIMAL(12, 6)    NULL,
    fetched_at       DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    deleted          BIT               NOT NULL DEFAULT 0,
    deleted_at       DATETIMEOFFSET(7) NULL,
    created_at       DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at       DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by       NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by       NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_fund_benchmark_point PRIMARY KEY (id),
    CONSTRAINT fk_fund_benchmark_point_fund FOREIGN KEY (fund_id) REFERENCES funds (id),
    CONSTRAINT uk_fund_benchmark_point_fund_date UNIQUE (fund_id, data_date)
);
