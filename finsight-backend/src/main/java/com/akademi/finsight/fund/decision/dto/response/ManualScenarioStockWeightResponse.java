package com.akademi.finsight.fund.decision.dto.response;

import java.math.BigDecimal;

public record ManualScenarioStockWeightResponse(
        String assetCode,
        BigDecimal targetWeight,
        BigDecimal currentWeight
) {}
