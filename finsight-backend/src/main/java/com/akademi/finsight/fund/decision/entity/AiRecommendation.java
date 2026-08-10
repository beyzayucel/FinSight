package com.akademi.finsight.fund.decision.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import com.akademi.finsight.fund.entity.*;
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
@Table(name = "ai_recommendation")
@SQLDelete(sql = "UPDATE ai_recommendation SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AiRecommendation extends SoftDeletableEntity implements ScenarioWeightSource, MetricsHolder {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RecommendationStatus status;

    @Column(nullable = false, length = 1000)
    private String rationale;

    @Column(name = "expected_risk_change", length = 255)
    private String expectedRiskChange;

    @Column(length = 500)
    private String note;

    // Karar Geçmişi ekranı için — hesaplama frontend'de yapılır, backend sadece saklar
    // (bkz. PerformanceMetrics: ManualScenario'da da aynı desen kullanılıyor).
    @Embedded
    @Builder.Default
    private PerformanceMetrics metrics = new PerformanceMetrics();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stress_test_result_id")
    private StressTestResult stressTestResult;

    @OneToMany(mappedBy = "recommendation", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapKey(name = "category")
    @MapKeyEnumerated(EnumType.STRING)
    @Builder.Default
    private Map<AssetCategory, AiRecommendationWeight> weights = new HashMap<>();

    @OneToMany(mappedBy = "recommendation", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapKey(name = "assetCode")
    @Builder.Default
    private Map<String, AiRecommendationStockWeight> stockWeights = new HashMap<>();

    public void addWeight(AiRecommendationWeight weight) {
        weights.put(weight.getCategory(), weight);
        weight.setRecommendation(this);
    }

    public void addStockWeight(AiRecommendationStockWeight stockWeight) {
        stockWeights.put(stockWeight.getAssetCode(), stockWeight);
        stockWeight.setRecommendation(this);
    }

    @Override
    public Map<AssetCategory, BigDecimal> getSimulationWeights() {
        Map<AssetCategory, BigDecimal> result = new EnumMap<>(AssetCategory.class);
        weights.forEach((cat, w) -> result.put(cat, w.getRecommendedWeight()));
        return result;
    }

    @Override
    public Map<String, BigDecimal> getSimulationStockWeights() {
        Map<String, BigDecimal> result = new HashMap<>();
        stockWeights.forEach((assetCode, w) -> result.put(assetCode, w.getRecommendedWeight()));
        return result;
    }
}
