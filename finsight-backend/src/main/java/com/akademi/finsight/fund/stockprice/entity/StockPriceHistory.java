package com.akademi.finsight.fund.stockprice.entity;

import com.akademi.finsight.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "stock_price_history", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_price_history_asset_date",
                columnNames = {"asset_code", "data_date"})
})
public class StockPriceHistory extends BaseEntity {

    @Column(name = "asset_code", nullable = false, length = 32)
    private String assetCode;

    @Column(name = "data_date", nullable = false)
    private LocalDate dataDate;

    @Column(name = "close_price", nullable = false, precision = 18, scale = 6)
    private BigDecimal closePrice;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}
