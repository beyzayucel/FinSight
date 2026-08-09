package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "manual_scenario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE manual_scenario SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class ManualScenario extends SoftDeletableEntity implements ScenarioWeightSource, MetricsHolder {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(length = 500)
    private String note;

    @Embedded
    @Builder.Default
    private PerformanceMetrics metrics = new PerformanceMetrics();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stress_test_result_id")
    private StressTestResult stressTestResult;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "category")
    @MapKeyEnumerated(EnumType.STRING)
    @Builder.Default
    private Map<AssetCategory, ManualScenarioWeight> weights = new HashMap<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "assetCode")
    @Builder.Default
    private Map<String, ManualScenarioStockWeight> stockWeights = new HashMap<>();

    public void addWeight(ManualScenarioWeight weight) {
        weights.put(weight.getCategory(), weight);
        weight.setScenario(this);
    }

    public void addStockWeight(ManualScenarioStockWeight stockWeight) {
        stockWeights.put(stockWeight.getAssetCode(), stockWeight);
        stockWeight.setScenario(this);
    }

    @Override
    public Map<AssetCategory, BigDecimal> getSimulationWeights() {
        Map<AssetCategory, BigDecimal> result = new EnumMap<>(AssetCategory.class);
        weights.forEach((cat, w) -> result.put(cat, w.getTargetWeight()));
        return result;
    }
}