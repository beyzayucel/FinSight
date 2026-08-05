package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "fund_distribution")
public class FundDistribution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_fund_distribution_fund"))
    private Fund fund;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal weight;

    @Column(nullable = false)
    private LocalDate date;
}
