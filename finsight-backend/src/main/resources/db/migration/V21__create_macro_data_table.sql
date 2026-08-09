IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'market_data')
BEGIN
    CREATE TABLE market_data (
        id                 UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
        data_date          DATE              NOT NULL,
        usd_return         DECIMAL(18, 12)   NOT NULL,
        gold_return        DECIMAL(18, 12)   NOT NULL,
        brent_return       DECIMAL(18, 12)   NOT NULL,
        us10y_return       DECIMAL(18, 12)   NOT NULL,
        cds_spread_bps     DECIMAL(18, 4)    NOT NULL,
        annual_inflation   DECIMAL(18, 4)    NOT NULL,
        policy_rate        DECIMAL(18, 4)    NOT NULL,
        deleted            BIT               NOT NULL DEFAULT 0,
        deleted_at         DATETIMEOFFSET(7) NULL,
        created_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
        updated_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
        created_by         NVARCHAR(255)     DEFAULT N'SYSTEM',
        updated_by         NVARCHAR(255)     DEFAULT N'SYSTEM',
        CONSTRAINT pk_market_data PRIMARY KEY (id),
        CONSTRAINT uk_market_data_data_date UNIQUE (data_date)
    );
END
