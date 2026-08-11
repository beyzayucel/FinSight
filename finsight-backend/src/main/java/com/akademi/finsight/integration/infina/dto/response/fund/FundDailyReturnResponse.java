package com.akademi.finsight.integration.infina.dto.response.fund;

import java.math.BigDecimal;
import java.util.List;

public record FundDailyReturnResponse(
        String fundCode,
        List<BigDecimal> dailyYields
) {}
