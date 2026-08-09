package com.akademi.finsight.fund.performancecomparison.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record PerformanceComparisonResponse(
        PortfolioCurve currentPortfolio,
        PortfolioCurve simulationPortfolio,
        PortfolioCurve benchmarkPortfolio,
        ScenarioSource scenarioSource,
        int analysisWindow,
        String dataDate
) {

    public record PortfolioCurve(
            List<CurvePoint> points,
            PortfolioMetrics metrics
    ) {}

    public record CurvePoint(
            LocalDate date,
            BigDecimal value
    ) {}

    public record PortfolioMetrics(
            BigDecimal currentValue,
            BigDecimal totalReturnPct,
            BigDecimal maxDrawdownPct,
            BigDecimal dailyVolatilityPct
    ) {}
}
