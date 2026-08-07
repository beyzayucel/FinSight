package com.akademi.finsight.fund.dto.response;

import com.akademi.finsight.fund.entity.AssetCategory;

import java.math.BigDecimal;

public record ManualScenarioWeightResponse(
        AssetCategory category,
        BigDecimal targetWeight,
        BigDecimal currentWeight
) {}
