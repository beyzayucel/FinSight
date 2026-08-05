ALTER TABLE manual_scenario
    ADD deleted    BIT               NOT NULL DEFAULT 0,
        deleted_at DATETIMEOFFSET(7) NULL;

ALTER TABLE manual_scenario_weight
    ADD deleted    BIT               NOT NULL DEFAULT 0,
        deleted_at DATETIMEOFFSET(7) NULL;
