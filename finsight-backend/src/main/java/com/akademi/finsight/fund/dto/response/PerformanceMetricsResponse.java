package com.akademi.finsight.fund.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformanceMetricsResponse(
        BigDecimal totalReturnPct,
        BigDecimal benchmarkDiffPct,
        BigDecimal maxDrawdownPct,
        BigDecimal dailyVolatilityPct,
        Integer analysisWindowDays,
        // Pencerenin bittiği veri tarihi (T-8) — karar tarihi değil. Eski kayıtlarda null.
        LocalDate dataDate
) {}
