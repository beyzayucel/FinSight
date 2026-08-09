package com.akademi.finsight.fund.performancecomparison.service.impl;

import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.entity.FundBenchmarkPoint;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.CurvePoint;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioCurve;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioMetrics;
import com.akademi.finsight.fund.performancecomparison.service.PerformanceComparisonService;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.performancecomparison.util.PortfolioCalculationUtil;
import com.akademi.finsight.fund.repository.FundBenchmarkPointRepository;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.fund.performancecomparison.service.impl.ScenarioResolver.ResolvedScenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceComparisonServiceImpl implements PerformanceComparisonService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String PERIOD_PREFIX = "P";
    private static final String PERIOD_SUFFIX = "D";

    private final FundPeriodMetricService fundPeriodMetricService;
    private final FundBenchmarkPointRepository fundBenchmarkPointRepository;
    private final PortfolioSimulationCalculationService simulationCalculationService;
    private final ScenarioResolver scenarioResolver;

    @Override
    public PerformanceComparisonResponse compare(String fundCode, int analysisWindow) {
        String period = PERIOD_PREFIX + analysisWindow + PERIOD_SUFFIX;

        FundPeriodMetricResponse metric = fundPeriodMetricService.getLatestByFundCodeAndPeriod(fundCode, period);
        LocalDate startDate = metric.dataDate().minusDays(analysisWindow);

        List<FundBenchmarkPoint> benchmarkPoints = fundBenchmarkPointRepository
                .findWindowByFundCode(fundCode, startDate, metric.dataDate());

        List<BigDecimal> fundCumulativeReturns = benchmarkPoints.stream()
                .map(FundBenchmarkPoint::getFundReturn)
                .toList();
        List<BigDecimal> benchmarkCumulativeReturns = benchmarkPoints.stream()
                .map(FundBenchmarkPoint::getBenchmarkReturn)
                .toList();
        List<LocalDate> dates = benchmarkPoints.stream()
                .map(FundBenchmarkPoint::getDataDate)
                .toList();

        PortfolioCurve currentPortfolio = buildPortfolio(
                fundCumulativeReturns, dates, metric.totalValue(), metric.cumulativeReturn());

        ResolvedScenario scenario = scenarioResolver.resolve(fundCode).orElse(null);
        PortfolioCurve simulationPortfolio = scenario != null
                ? simulationCalculationService.calculateSimulation(fundCode, analysisWindow, scenario.weights())
                : null;

        BigDecimal benchmarkCurrentValue = calculateBenchmarkValue(
                metric.totalValue(), metric.cumulativeReturn(), metric.benchmarkReturn());
        PortfolioCurve benchmarkPortfolio = buildPortfolio(
                benchmarkCumulativeReturns, dates, benchmarkCurrentValue, metric.benchmarkReturn());

        return PerformanceComparisonResponse.builder()
                .currentPortfolio(currentPortfolio)
                .simulationPortfolio(simulationPortfolio)
                .benchmarkPortfolio(benchmarkPortfolio)
                .scenarioSource(scenario != null ? scenario.source() : null)
                .analysisWindow(analysisWindow)
                .dataDate(metric.dataDate().format(DATE_FORMAT))
                .build();
    }

    private PortfolioCurve buildPortfolio(List<BigDecimal> cumulativeReturns,
                                          List<LocalDate> dates,
                                          BigDecimal currentValue,
                                          BigDecimal totalReturnPct) {
        if (cumulativeReturns.isEmpty()) {
            return null;
        }

        List<CurvePoint> curve = buildCurveFromCumulative(cumulativeReturns, dates);

        List<BigDecimal> dailyReturns = PortfolioCalculationUtil
                .deriveDailyReturnsFromCumulative(cumulativeReturns);

        BigDecimal maxDrawdownPct = PortfolioCalculationUtil.calculateMaxDrawdown(dailyReturns);
        BigDecimal dailyVolatilityPct = PortfolioCalculationUtil.calculateDailyVolatility(dailyReturns);

        PortfolioMetrics metrics = new PortfolioMetrics(
                currentValue, totalReturnPct, maxDrawdownPct, dailyVolatilityPct);

        return new PortfolioCurve(curve, metrics);
    }

    /**
     * Benchmark'a yatırılsaydı portföy bugün kaç TL olurdu?
     *
     * <pre>
     *   investedAmount = totalValue / (1 + fundReturn%)
     *   benchmarkValue = investedAmount × (1 + benchmarkReturn%)
     *
     *   Örnek: totalValue=1.056.679, fundReturn=%5.67, benchmarkReturn=%5.67
     *          investedAmount = 1.056.679 / 1.0567 = 1.000.000
     *          benchmarkValue = 1.000.000 × 1.0567 = 1.056.700
     * </pre>
     */
    private BigDecimal calculateBenchmarkValue(BigDecimal totalValue,
                                                BigDecimal fundReturnPct,
                                                BigDecimal benchmarkReturnPct) {
        if (fundReturnPct == null || benchmarkReturnPct == null) {
            return totalValue;
        }

        BigDecimal fundRate = fundReturnPct.movePointLeft(2);
        BigDecimal benchmarkRate = benchmarkReturnPct.movePointLeft(2);

        BigDecimal investedAmount = totalValue.divide(
                BigDecimal.ONE.add(fundRate), 6, RoundingMode.HALF_UP);

        return investedAmount.multiply(BigDecimal.ONE.add(benchmarkRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Kümülatif getiri serisini grafik için sıfır tabanlı eğriye dönüştürür.
     * İlk noktayı 0 kabul edip tüm değerlerden çıkarır (rebase).
     *
     * <pre>
     *   Örnek: kümülatifler = [2.5, 3.0, 4.1, 3.8]
     *          rebase → ilk değer 2.5 çıkarılır
     *          eğri   = [0.0, 0.5, 1.6, 1.3]
     *
     *   Grafik böylece 0'dan başlar, yukarı/aşağı hareketler görünür.
     * </pre>
     */
    private List<CurvePoint> buildCurveFromCumulative(List<BigDecimal> cumulativeReturns,
                                                      List<LocalDate> dates) {
        if (cumulativeReturns.isEmpty()) {
            return List.of();
        }

        BigDecimal baseReturn = cumulativeReturns.getFirst();

        return IntStream.range(0, cumulativeReturns.size())
                .mapToObj(i -> {
                    BigDecimal rebased = cumulativeReturns.get(i).subtract(baseReturn);
                    return new CurvePoint(dates.get(i), rebased);
                })
                .toList();
    }
}
