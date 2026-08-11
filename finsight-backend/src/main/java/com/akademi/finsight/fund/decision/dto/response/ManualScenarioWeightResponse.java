package com.akademi.finsight.fund.decision.dto.response;

import com.akademi.finsight.fund.decision.entity.AssetCategory;

import java.math.BigDecimal;

public record ManualScenarioWeightResponse(
        AssetCategory category,
        BigDecimal targetWeight,
        BigDecimal currentWeight
) {}
