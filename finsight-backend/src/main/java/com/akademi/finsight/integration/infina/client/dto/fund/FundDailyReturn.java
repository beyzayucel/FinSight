package com.akademi.finsight.integration.infina.client.dto.fund;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FundDailyReturn(
        String fundCode,
        List<BigDecimal> dailyYields
) {}
