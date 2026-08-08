package com.akademi.finsight.stresstest.dto.response;

import java.math.BigDecimal;

//TODO: AI modelinden gelecek veriler için kullanılacak gerekli eklemeler yapılacak
public record FastApiInferenceResponseDto(
        BigDecimal expectedImpactRate,
        BigDecimal postShockValue
) {
}
