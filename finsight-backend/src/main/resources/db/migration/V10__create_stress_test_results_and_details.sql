CREATE TABLE stress_test_results
(
    id              UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    user_id         UNIQUEIDENTIFIER  NOT NULL,
    fund_id         UNIQUEIDENTIFIER  NOT NULL,
    simulation_type NVARCHAR(50)      NOT NULL,

    deleted         BIT               NOT NULL DEFAULT 0,
    deleted_at      DATETIMEOFFSET(7),

    created_at      DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at      DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by      NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by      NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_stress_test_results PRIMARY KEY (id),

    CONSTRAINT fk_stress_test_result_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_stress_test_result_fund
        FOREIGN KEY (fund_id)
            REFERENCES funds(id)
);

CREATE INDEX idx_stress_test_result_user_id
    ON stress_test_results(user_id);

CREATE INDEX idx_stress_test_result_fund_id
    ON stress_test_results(fund_id);


CREATE TABLE stress_test_result_detail
(
    id                      UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    stress_test_result_id   UNIQUEIDENTIFIER  NOT NULL,

    portfolio_type          NVARCHAR(50)      NOT NULL,

    initial_value           DECIMAL(19,2),
    expected_impact_rate    DECIMAL(8,4),
    post_shock_value        DECIMAL(19,2),

    deleted                 BIT               NOT NULL DEFAULT 0,
    deleted_at              DATETIMEOFFSET(7),

    created_at              DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at              DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by              NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by              NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_stress_test_result_detail PRIMARY KEY (id),

    CONSTRAINT fk_stress_test_result_detail_result
        FOREIGN KEY (stress_test_result_id)
            REFERENCES stress_test_results(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_stress_test_result_detail_result_id
    ON stress_test_result_detail(stress_test_result_id);