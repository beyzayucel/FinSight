package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "market_data", uniqueConstraints = {
        @UniqueConstraint(name = "uk_market_data_data_date", columnNames = {"data_date"})
})
@SQLDelete(sql = "UPDATE market_data SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class MarketData extends SoftDeletableEntity {

    @Column(name = "data_date", nullable = false)
    private LocalDate dataDate;

    @Column(name = "usd_return", nullable = false, precision = 18, scale = 12)
    private BigDecimal usdReturn;

    @Column(name = "gold_return", nullable = false, precision = 18, scale = 12)
    private BigDecimal goldReturn;

    @Column(name = "brent_return", nullable = false, precision = 18, scale = 12)
    private BigDecimal brentReturn;

    @Column(name = "us10y_return", nullable = false, precision = 18, scale = 12)
    private BigDecimal us10yReturn;

    @Column(name = "cds_spread_bps", nullable = false, precision = 18, scale = 4)
    private BigDecimal cdsSpreadBps;

    @Column(name = "annual_inflation", nullable = false, precision = 18, scale = 4)
    private BigDecimal annualInflation;

    @Column(name = "policy_rate", nullable = false, precision = 18, scale = 4)
    private BigDecimal policyRate;
}
