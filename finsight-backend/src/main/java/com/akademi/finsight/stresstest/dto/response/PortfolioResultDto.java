package com.akademi.finsight.stresstest.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PortfolioResultDto(
        BigDecimal initialValue,
        BigDecimal expectedImpactRate,
        BigDecimal postShockValue
) {}
