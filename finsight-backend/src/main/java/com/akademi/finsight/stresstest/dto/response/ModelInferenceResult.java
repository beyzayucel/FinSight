package com.akademi.finsight.stresstest.dto.response;

import java.math.BigDecimal;

public record ModelInferenceResult(
        BigDecimal expectedImpactRate,
        BigDecimal postShockValue
) {
}
