package com.akademi.finsight.fund.dto.response;

import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.entity.RecommendationStatus;

import java.util.Map;
import java.util.UUID;

public record AIRecommendationResponse(
        UUID id,
        UUID fundId,
        RecommendationStatus status,
        String rationale,
        String expectedRiskChange,
        String note,
        Map<AssetCategory, AiWeightResponse> weights
) {}
