ALTER TABLE fund_period_metric
    ADD benchmark_return DECIMAL(12, 6) NULL;

ALTER TABLE fund_period_metric
    DROP COLUMN benchmark_diff_bps;
