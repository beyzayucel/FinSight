CREATE TABLE manual_scenario_stock_weight
(
    id             UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    scenario_id    UNIQUEIDENTIFIER  NOT NULL,
    asset_code     NVARCHAR(32)      NOT NULL,
    target_weight  DECIMAL(5, 2)     NOT NULL,
    current_weight DECIMAL(5, 2)     NOT NULL,
    deleted        BIT               NOT NULL DEFAULT 0,
    deleted_at     DATETIMEOFFSET(7) NULL,
    created_at     DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at     DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by     NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by     NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_manual_scenario_stock_weight PRIMARY KEY (id),
    CONSTRAINT fk_manual_scenario_stock_weight_scenario FOREIGN KEY (scenario_id) REFERENCES manual_scenario (id) ON DELETE CASCADE
);

CREATE INDEX idx_manual_scenario_stock_weight_sc_id ON manual_scenario_stock_weight (scenario_id);
