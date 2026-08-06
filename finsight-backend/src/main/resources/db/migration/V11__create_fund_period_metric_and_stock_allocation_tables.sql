CREATE TABLE fund_period_metric
(
    id                 UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    fund_id            UNIQUEIDENTIFIER  NOT NULL,
    data_date          DATE              NOT NULL,
    period             NVARCHAR(16)      NOT NULL,
    total_value        DECIMAL(19, 4)    NOT NULL,
    daily_return       DECIMAL(9, 6)     NOT NULL,
    cumulative_return  DECIMAL(12, 6)    NOT NULL,
    benchmark_diff_bps INT               NULL,
    fetched_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    deleted            BIT               NOT NULL DEFAULT 0,
    deleted_at         DATETIMEOFFSET(7) NULL,
    created_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by         NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by         NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_fund_period_metric PRIMARY KEY (id),
    CONSTRAINT fk_fund_period_metric_fund FOREIGN KEY (fund_id) REFERENCES funds (id),
    CONSTRAINT uk_fund_period_metric_fund_date_period UNIQUE (fund_id, data_date, period)
);


CREATE TABLE fund_stock_allocation
(
    id         UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    fund_id    UNIQUEIDENTIFIER  NOT NULL,
    period     NVARCHAR(7)       NOT NULL,
    asset_code NVARCHAR(32)      NOT NULL,
    weight     DECIMAL(9, 6)     NOT NULL,
    deleted    BIT               NOT NULL DEFAULT 0,
    deleted_at DATETIMEOFFSET(7) NULL,
    created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_fund_stock_allocation PRIMARY KEY (id),
    CONSTRAINT fk_fund_stock_allocation_fund FOREIGN KEY (fund_id) REFERENCES funds (id),
    CONSTRAINT uk_fund_stock_allocation_fund_period_asset UNIQUE (fund_id, period, asset_code)
);
