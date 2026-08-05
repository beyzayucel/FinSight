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
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "fund_distribution", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fund_distribution_fund_category_date",
                columnNames = {"fund_id", "category", "date"})
})
@SQLDelete(sql = "UPDATE fund_distribution SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class FundDistribution extends SoftDeletableEntity {

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
