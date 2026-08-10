-- stock_price_history salt bir fiyat cache'i; soft-delete anlamsiz (audit/geri getirme ihtiyaci yok).
-- Eski pencere disina cikan kayitlar fiziksel olarak silinir, DB'nin siskin buyumesi boylece onlenir.
CREATE TABLE stock_price_history
(
    id          UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
    asset_code  NVARCHAR(32)      NOT NULL,
    data_date   DATE              NOT NULL,
    close_price DECIMAL(18, 6)    NOT NULL,
    fetched_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at  DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by  NVARCHAR(255)     DEFAULT N'SYSTEM',
    updated_by  NVARCHAR(255)     DEFAULT N'SYSTEM',

    CONSTRAINT pk_stock_price_history PRIMARY KEY (id),
    CONSTRAINT uk_stock_price_history_asset_date UNIQUE (asset_code, data_date)
);
