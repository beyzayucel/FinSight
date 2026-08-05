package com.akademi.finsight.stresstest.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PortfolioResultDto(
        BigDecimal initialValue,      // Şok öncesi değer
        BigDecimal expectedImpactRate, // Beklenen etki oranı (Örn: -0.0952 -> % -9.52)
        BigDecimal postShockValue     // Şok sonrası değer
) {}
