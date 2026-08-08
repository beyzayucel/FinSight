package com.akademi.finsight.fund.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "market_data")
public class MarketData {

    @Id
    @Column(nullable = false)
    private LocalDate date;

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
