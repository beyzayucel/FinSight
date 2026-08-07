package com.akademi.finsight.fund.entity;

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
@Table(name = "ai_recommendation_weight")
@SQLDelete(sql = "UPDATE ai_recommendation_weight SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AiRecommendationWeight extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private AiRecommendation recommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCategory category;

    @Column(name = "recommended_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal recommendedWeight;

    @Column(name = "current_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal currentWeight;
}
