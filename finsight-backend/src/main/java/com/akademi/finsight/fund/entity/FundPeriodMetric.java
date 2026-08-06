package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "fund_period_metric", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fund_period_metric_fund_date_period",
                columnNames = {"fund_id", "data_date", "period"})
})
@SQLDelete(sql = "UPDATE fund_period_metric SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class FundPeriodMetric extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_fund_period_metric_fund"))
    private Fund fund;

    @Column(name = "data_date", nullable = false)
    private LocalDate dataDate;

    @Column(nullable = false, length = 16)
    private String period;

    @Column(name = "total_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalValue;

    @Column(name = "previous_total_value", precision = 19, scale = 4)
    private BigDecimal previousTotalValue;

    @Column(name = "previous_date")
    private LocalDate previousDate;

    @Column(name = "daily_return", nullable = false, precision = 9, scale = 6)
    private BigDecimal dailyReturn;

    @Column(name = "cumulative_return", nullable = false, precision = 12, scale = 6)
    private BigDecimal cumulativeReturn;

    @Column(name = "benchmark_return", precision = 12, scale = 6)
    private BigDecimal benchmarkReturn;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}
