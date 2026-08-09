CREATE TABLE ai_recommendation_stock_weight
(
    id                 UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    recommendation_id  UNIQUEIDENTIFIER  NOT NULL,
    asset_code         NVARCHAR(32)      NOT NULL,
    recommended_weight DECIMAL(5, 2)     NOT NULL,
    current_weight     DECIMAL(5, 2)     NOT NULL,
    deleted            BIT               NOT NULL DEFAULT 0,
    deleted_at         DATETIMEOFFSET(7) NULL,
    created_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at         DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by         NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by         NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_ai_recommendation_stock_weight PRIMARY KEY (id),
    CONSTRAINT fk_ai_recommendation_stock_weight_rec FOREIGN KEY (recommendation_id) REFERENCES ai_recommendation (id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_recommendation_stock_weight_rec_id ON ai_recommendation_stock_weight (recommendation_id);
