ALTER TABLE funds
    DROP COLUMN [date];

ALTER TABLE fund_period_metric
    ADD previous_total_value DECIMAL(19, 4) NULL;
