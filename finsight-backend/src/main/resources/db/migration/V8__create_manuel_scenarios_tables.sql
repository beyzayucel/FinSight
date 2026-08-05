-- 1. Manuel Senaryo Tablosu
CREATE TABLE manual_scenario
(
    id          UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    user_id     UNIQUEIDENTIFIER  NOT NULL,
    fund_id     UNIQUEIDENTIFIER  NOT NULL,
    note        NVARCHAR(500),
    created_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by  NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by  NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_manual_scenario PRIMARY KEY (id),
    CONSTRAINT fk_manual_scenario_users FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_manual_scenario_funds FOREIGN KEY (fund_id) REFERENCES funds (id)
);

CREATE INDEX idx_manual_scenario_user_id ON manual_scenario (user_id);
CREATE INDEX idx_manual_scenario_fund_id ON manual_scenario (fund_id);


-- 2. Manuel Senaryo Ağırlıkları Tablosu
CREATE TABLE manual_scenario_weight
(
    id            UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    scenario_id   UNIQUEIDENTIFIER  NOT NULL,
    category      NVARCHAR(50)      NOT NULL,
    target_weight DECIMAL(5, 2)     NOT NULL,
    current_weight DECIMAL(5, 2)     NOT NULL,
    created_at    DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at    DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by    NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by    NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_manual_scenario_weight PRIMARY KEY (id),
    CONSTRAINT fk_manual_scenario_weight_scenario FOREIGN KEY (scenario_id) REFERENCES manual_scenario (id) ON DELETE CASCADE
);

CREATE INDEX idx_manual_scenario_weight_sc_id ON manual_scenario_weight (scenario_id);