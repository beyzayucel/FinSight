package com.akademi.finsight.stresstest.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ModelInferenceResult(
        BigDecimal expectedImpactRate,
        BigDecimal postShockValue
) {

}
