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
@Table(name = "fund_benchmark_point", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fund_benchmark_point_fund_date",
                columnNames = {"fund_id", "data_date"})
})
@SQLDelete(sql = "UPDATE fund_benchmark_point SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class FundBenchmarkPoint extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_fund_benchmark_point_fund"))
    private Fund fund;

    @Column(name = "data_date", nullable = false)
    private LocalDate dataDate;

    @Column(name = "fund_return", nullable = false, precision = 12, scale = 6)
    private BigDecimal fundReturn;

    @Column(name = "benchmark_return", precision = 12, scale = 6)
    private BigDecimal benchmarkReturn;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}
