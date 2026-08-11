package com.akademi.finsight.fund.decision.dto.response;

import java.math.BigDecimal;

public record AIRecommendationStockWeightResponse(
        String assetCode,
        BigDecimal recommendedWeight,
        BigDecimal currentWeight
) {
}
