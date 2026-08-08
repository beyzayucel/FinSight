package com.akademi.finsight.fund.performancecomparison.service.impl;

import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.CurvePoint;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioCurve;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioMetrics;
import com.akademi.finsight.fund.performancecomparison.service.PerformanceComparisonService;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.performancecomparison.util.PortfolioCalculationUtil;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.fund.performancecomparison.service.impl.ScenarioResolver.ResolvedScenario;
import com.akademi.finsight.integration.infina.dto.response.fund.FundDailyReturnResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceComparisonServiceImpl implements PerformanceComparisonService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String PERIOD_PREFIX = "P";
    private static final String PERIOD_SUFFIX = "D";

    private final InfinaService infinaService;
    private final FundPeriodMetricService fundPeriodMetricService;
    private final PortfolioSimulationCalculationService simulationCalculationService;
    private final ScenarioResolver scenarioResolver;

    @Override
    public PerformanceComparisonResponse compare(String fundCode, int analysisWindow) {
        String period = PERIOD_PREFIX + analysisWindow + PERIOD_SUFFIX;

        FundPeriodMetricResponse metric = fundPeriodMetricService.getLatestByFundCodeAndPeriod(fundCode, period);
        List<BigDecimal> dailyYields = fetchDailyYields(fundCode, analysisWindow, metric.dataDate());

        PortfolioCurve currentPortfolio = buildCurrentPortfolio(metric, dailyYields);

        ResolvedScenario scenario = scenarioResolver.resolve(fundCode).orElse(null);
        PortfolioCurve simulationPortfolio = scenario != null
                ? simulationCalculationService.calculateSimulation(fundCode, analysisWindow, scenario.weights())
                : null;

        PortfolioCurve benchmarkPortfolio = buildBenchmarkPortfolio(
                metric.benchmarkReturn(), dailyYields.size(), metric.totalValue());

        return new PerformanceComparisonResponse(
                currentPortfolio,
                simulationPortfolio,
                benchmarkPortfolio,
                scenario != null ? scenario.source() : null,
                analysisWindow,
                metric.dataDate().format(DATE_FORMAT)
        );
    }

    private List<BigDecimal> fetchDailyYields(String fundCode, int analysisWindow, LocalDate dataDate) {
        LocalDate startDate = dataDate.minusDays(analysisWindow);
        String dates = startDate.format(DATE_FORMAT) + "," + dataDate.format(DATE_FORMAT);
        FundDailyReturnResponse response = infinaService.getFundDailyReturn(fundCode, dates);
        return response.dailyYields();
    }

    private PortfolioCurve buildCurrentPortfolio(FundPeriodMetricResponse metric,
                                                  List<BigDecimal> dailyYields) {
        List<CurvePoint> curve = PortfolioCalculationUtil.buildCumulativeCurve(dailyYields);

        BigDecimal maxDrawdownPct = PortfolioCalculationUtil.calculateMaxDrawdown(dailyYields);
        BigDecimal dailyVolatilityPct = PortfolioCalculationUtil.calculateDailyVolatility(dailyYields);
        PortfolioMetrics metrics = new PortfolioMetrics(
                metric.totalValue(), metric.cumulativeReturn(), maxDrawdownPct, dailyVolatilityPct);

        return new PortfolioCurve(curve, metrics);
    }

    // TODO: Infina benchmark günlük getiri API'si gelince interpolasyon yerine gerçek veri kullanılacak.
    private PortfolioCurve buildBenchmarkPortfolio(BigDecimal benchmarkReturnPct,
                                                    int dayCount,
                                                    BigDecimal portfolioValue) {
        if (benchmarkReturnPct == null || dayCount == 0) {
            log.debug("No benchmark data available");
            return null;
        }

        List<BigDecimal> benchmarkDailyYields = PortfolioCalculationUtil
                .interpolateBenchmarkDailyYields(benchmarkReturnPct, dayCount);

        List<CurvePoint> curve = PortfolioCalculationUtil.buildCumulativeCurve(benchmarkDailyYields);
        PortfolioMetrics metrics = PortfolioCalculationUtil
                .buildMetricsFromYields(benchmarkDailyYields, portfolioValue);

        return new PortfolioCurve(curve, metrics);
    }

}
