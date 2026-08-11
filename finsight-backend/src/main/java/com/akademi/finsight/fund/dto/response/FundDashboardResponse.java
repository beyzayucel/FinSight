package com.akademi.finsight.fund.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FundDashboardResponse(
        FundInfo fund,
        BigDecimal totalValue,
        BigDecimal dailyReturn,
        List<PeriodMetrics> periods,
        List<CategoryWeight> distribution,
        FundStockBreakdownResponse stockBreakdown
) {

    public record FundInfo(String code, String name, LocalDate dataDate) {}

    public record PeriodMetrics(
            String code,
            BigDecimal previousTotalValue,
            LocalDate previousDate,
            Integer days,
            BigDecimal change,
            BigDecimal changePercent,
            BigDecimal cumulativeReturn,
            BigDecimal benchmarkReturn,
            BigDecimal benchmarkDiffBps,
            List<SeriesPoint> series
    ) {}

    public record SeriesPoint(LocalDate date, BigDecimal fund, BigDecimal benchmark) {}

    public record CategoryWeight(String category, BigDecimal weight) {}
}
