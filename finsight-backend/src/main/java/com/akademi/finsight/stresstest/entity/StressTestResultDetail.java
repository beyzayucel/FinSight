package com.akademi.finsight.stresstest.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "stress_test_result_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE stress_test_result_detail SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class StressTestResultDetail extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stress_test_result_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stress_test_result_detail_result"))
    private StressTestResult stressTestResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "portfolio_type", nullable = false)
    private PortfolioType portfolioType;

    @Column(name = "initial_value", precision = 19, scale = 2)
    private BigDecimal initialValue;

    @Column(name = "expected_impact_rate", precision = 8, scale = 4)
    private BigDecimal expectedImpactRate;

    @Column(name = "post_shock_value", precision = 19, scale = 2)
    private BigDecimal postShockValue;
}
