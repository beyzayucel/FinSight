package com.akademi.finsight.fund.dto.sync;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FundSyncSnapshot(
        String fundCode,
        String fundName,
        LocalDate dataDate,
        BigDecimal totalValue,
        BigDecimal dailyReturn,
        List<PeriodMetric> periodMetrics,
        List<BenchmarkPoint> benchmarkPoints,
        List<Distribution> distributions,
        String allocationPeriod,
        List<StockAllocation> stockAllocations
) {

    public record PeriodMetric(
            String period,
            LocalDate previousDate,
            BigDecimal previousTotalValue,
            BigDecimal cumulativeReturn,
            BigDecimal benchmarkReturn
    ) {}

    public record BenchmarkPoint(
            LocalDate date,
            BigDecimal fundReturn,
            BigDecimal benchmarkReturn
    ) {}

    public record Distribution(String category, BigDecimal weight) {}

    public record StockAllocation(String assetCode, BigDecimal weight) {}
}
