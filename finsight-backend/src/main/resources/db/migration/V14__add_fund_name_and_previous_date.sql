ALTER TABLE funds
    ADD name NVARCHAR(255) NULL;

ALTER TABLE fund_period_metric
    ADD previous_date DATE NULL;
