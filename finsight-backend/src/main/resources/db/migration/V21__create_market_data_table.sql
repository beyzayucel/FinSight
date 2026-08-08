IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'market_data')
BEGIN
    CREATE TABLE market_data (
        date DATE PRIMARY KEY,
        usd_return DECIMAL(18, 12) NOT NULL,
        gold_return DECIMAL(18, 12) NOT NULL,
        brent_return DECIMAL(18, 12) NOT NULL,
        us10y_return DECIMAL(18, 12) NOT NULL,
        cds_spread_bps DECIMAL(18, 4) NOT NULL,
        annual_inflation DECIMAL(18, 4) NOT NULL,
        policy_rate DECIMAL(18, 4) NOT NULL
    );
END
