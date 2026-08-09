package com.akademi.finsight.fund.dto.response;

import java.math.BigDecimal;

public record AIRecommendationStockWeightResponse(
        String assetCode,
        BigDecimal recommendedWeight,
        BigDecimal currentWeight
) {
}
