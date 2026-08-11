package com.akademi.finsight.stresstest.dto.request;

import java.math.BigDecimal;
import java.util.Map;

public record FastApiInferenceRequestDto(
        String scenarioKey,
        BigDecimal initialValue,
        Map<String, Float> assetWeights
) {
}
