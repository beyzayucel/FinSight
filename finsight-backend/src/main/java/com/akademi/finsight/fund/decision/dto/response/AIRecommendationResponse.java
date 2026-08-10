package com.akademi.finsight.fund.decision.dto.response;

import com.akademi.finsight.fund.decision.entity.AssetCategory;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;

import lombok.Builder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record AIRecommendationResponse(
        UUID id,
        UUID fundId,
        RecommendationStatus status,
        String rationale,
        String expectedRiskChange,
        String note,
        Map<AssetCategory, AiWeightResponse> weights,
        List<AIRecommendationStockWeightResponse> stockWeights
) {}
