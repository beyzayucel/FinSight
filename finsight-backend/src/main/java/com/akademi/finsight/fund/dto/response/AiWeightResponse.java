package com.akademi.finsight.fund.dto.response;

import java.math.BigDecimal;

public record AiWeightResponse(
        BigDecimal recommendedWeight,
        BigDecimal currentWeight
) {}
