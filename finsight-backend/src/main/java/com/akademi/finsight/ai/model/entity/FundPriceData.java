package com.akademi.finsight.ai.model.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import com.akademi.finsight.fund.entity.Fund;
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
@Table(name = "fund_price_data", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fund_price_data_fund_date",
                columnNames = {"fund_id", "data_date"})
})
@SQLDelete(sql = "UPDATE fund_price_data SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class FundPriceData extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_fund_price_data_fund"))
    private Fund fund;

    @Column(name = "data_date", nullable = false)
    private LocalDate dataDate;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal price;

    @Column(name = "active_value", precision = 19, scale = 4)
    private BigDecimal activeValue;

    @Column(name = "portfolio_value", precision = 19, scale = 4)
    private BigDecimal portfolioValue;

    @Column(name = "cash_value", precision = 19, scale = 4)
    private BigDecimal cashValue;

    @Column(name = "investor_count", precision = 19, scale = 4)
    private BigDecimal investorCount;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}
