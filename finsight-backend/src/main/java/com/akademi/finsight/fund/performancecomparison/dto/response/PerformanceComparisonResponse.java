package com.akademi.finsight.fund.performancecomparison.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
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
            int day,
            BigDecimal value
    ) {}

    public record PortfolioMetrics(
            BigDecimal currentValue,
            BigDecimal totalReturnPct,
            BigDecimal maxDrawdownPct,
            BigDecimal dailyVolatilityPct
    ) {}
}
