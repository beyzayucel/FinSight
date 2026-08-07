package com.akademi.finsight.fund.dto.response;

import java.math.BigDecimal;

public record PerformanceMetricsResponse(
        BigDecimal totalReturnPct,
        BigDecimal benchmarkDiffPct,
        BigDecimal maxDrawdownPct,
        BigDecimal dailyVolatilityPct,
        Integer analysisWindowDays
) {}
