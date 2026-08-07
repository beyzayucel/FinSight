-- 1. AI Recommendation Table
CREATE TABLE ai_recommendation
(
    id                   UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    user_id              UNIQUEIDENTIFIER  NOT NULL,
    fund_id              UNIQUEIDENTIFIER  NOT NULL,
    status               NVARCHAR(50)      NOT NULL,
    rationale            NVARCHAR(1000)    NOT NULL,
    expected_risk_change NVARCHAR(255),
    note                 NVARCHAR(1000),
    created_at           DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at           DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by           NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by           NVARCHAR(255)     DEFAULT N'SYSTEM',
    deleted              BIT               NOT NULL DEFAULT 0,
    deleted_at           DATETIMEOFFSET(7) NULL,

    CONSTRAINT pk_ai_recommendation PRIMARY KEY (id),
    CONSTRAINT fk_ai_recommendation_users FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_ai_recommendation_funds FOREIGN KEY (fund_id) REFERENCES funds (id)
);

CREATE INDEX idx_ai_recommendation_user_id ON ai_recommendation (user_id);
CREATE INDEX idx_ai_recommendation_fund_id ON ai_recommendation (fund_id);

-- 2. AI Recommendation Weights Table
CREATE TABLE ai_recommendation_weight
(
    id                 UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    recommendation_id  UNIQUEIDENTIFIER NOT NULL,
    category           NVARCHAR(50)     NOT NULL,
    recommended_weight DECIMAL(5, 2)    NOT NULL,
    current_weight     DECIMAL(5, 2)    NOT NULL,
    created_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by         NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by         NVARCHAR(255)     DEFAULT N'SYSTEM',
    deleted            BIT               NOT NULL DEFAULT 0,
    deleted_at         DATETIMEOFFSET(7) NULL,

    CONSTRAINT pk_ai_recommendation_weight PRIMARY KEY (id),
    CONSTRAINT fk_ai_recommendation_weight_rec FOREIGN KEY (recommendation_id) REFERENCES ai_recommendation (id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_rec_weight_rec_id ON ai_recommendation_weight (recommendation_id);
