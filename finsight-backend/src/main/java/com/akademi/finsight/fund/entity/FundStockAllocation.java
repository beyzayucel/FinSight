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

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "fund_stock_allocation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fund_stock_allocation_fund_period_asset",
                columnNames = {"fund_id", "period", "asset_code"})
})
@SQLDelete(sql = "UPDATE fund_stock_allocation SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class FundStockAllocation extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_fund_stock_allocation_fund"))
    private Fund fund;

    @Column(nullable = false, length = 7)
    private String period;

    @Column(name = "asset_code", nullable = false, length = 32)
    private String assetCode;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal weight;
}
