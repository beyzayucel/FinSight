ALTER TABLE funds
    ADD deleted    BIT               NOT NULL DEFAULT 0,
        deleted_at DATETIMEOFFSET(7) NULL;

ALTER TABLE fund_distribution
    ADD deleted    BIT               NOT NULL DEFAULT 0,
        deleted_at DATETIMEOFFSET(7) NULL;

ALTER TABLE fund_distribution
    ADD CONSTRAINT uk_fund_distribution_fund_category_date UNIQUE (fund_id, category, [date]);
