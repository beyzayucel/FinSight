package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import com.akademi.finsight.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

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
public class AiRecommendation extends SoftDeletableEntity {

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

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "category")
    @MapKeyEnumerated(EnumType.STRING)
    @Builder.Default
    private Map<AssetCategory, AiRecommendationWeight> weights = new HashMap<>();

    public void addWeight(AiRecommendationWeight weight) {
        weights.put(weight.getCategory(), weight);
        weight.setRecommendation(this);
    }
}
