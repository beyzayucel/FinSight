package com.akademi.finsight.fund.decision.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "ai_recommendation_stock_weight")
@SQLDelete(sql = "UPDATE ai_recommendation_stock_weight SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AiRecommendationStockWeight extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private AiRecommendation recommendation;

    @Column(name = "asset_code", nullable = false, length = 32)
    private String assetCode;

    @Column(name = "recommended_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal recommendedWeight;

    @Column(name = "current_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal currentWeight;
}
