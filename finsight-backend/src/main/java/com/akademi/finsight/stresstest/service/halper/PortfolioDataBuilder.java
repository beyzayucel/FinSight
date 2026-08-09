package com.akademi.finsight.stresstest.service.halper;

import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.performancecomparison.service.impl.ScenarioResolver;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioDataBuilder {
    private static final String PERIOD_PREFIX = "P";
    private static final String PERIOD_SUFFIX = "D";

    private static final Map<String, Float> DEFAULT_SIMULATION_WEIGHTS = Map.of(
            "EQUITY", 0.40f,
            "BOND", 0.30f,
            "FX", 0.15f,
            "CASH", 0.15f
    );

    private static final Map<String, Float> DEFAULT_BENCHMARK_WEIGHTS = Map.of(
            "EQUITY", 0.50f,
            "BOND", 0.25f,
            "FX", 0.15f,
            "CASH", 0.10f
    );

    private final ScenarioResolver scenarioResolver;
    private final PortfolioSimulationCalculationService simulationCalculationService;
    private final FundPeriodMetricService fundPeriodMetricService;

    public PortfolioDataDto buildSimulationPortfolio(String fundCode, int analysisWindow, BigDecimal initialValue) {
        try {
            ScenarioResolver.ResolvedScenario scenario = scenarioResolver.resolve(fundCode).orElse(null);

            if (scenario != null && scenario.weights() != null && !scenario.weights().isEmpty()) {
                PerformanceComparisonResponse.PortfolioCurve simulationCurve = simulationCalculationService
                        .calculateSimulation(fundCode, analysisWindow, scenario.weights());

                BigDecimal simulatedValue = (simulationCurve != null && simulationCurve.metrics() != null)
                        ? simulationCurve.metrics().currentValue()
                        : initialValue;

                Map<String, Float> convertedWeights = scenario.weights().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().name(),
                                e -> e.getValue() != null ? e.getValue().floatValue() : 0.0f
                        ));

                return createPortfolioData(simulatedValue, convertedWeights);
            }
        } catch (Exception e) {
            log.warn("Infina veya simülasyon servisi çağrısı başarısız oldu, varsayılan ağırlıklar kullanılıyor. Fon: {}, Hata: {}", fundCode, e.getMessage());
        }

        return createPortfolioData(initialValue, DEFAULT_SIMULATION_WEIGHTS);
    }

    public PortfolioDataDto buildBenchmarkPortfolio(String fundCode, int analysisWindow, BigDecimal initialValue) {
        try {
            String period = PERIOD_PREFIX + analysisWindow + PERIOD_SUFFIX;
            FundPeriodMetricResponse metric = fundPeriodMetricService.getLatestByFundCodeAndPeriod(fundCode, period);

            BigDecimal benchmarkCurrentValue = computeBenchmarkValue(
                    initialValue != null ? initialValue : metric.totalValue(),
                    metric.cumulativeReturn(),
                    metric.benchmarkReturn()
            );

            return createPortfolioData(benchmarkCurrentValue, DEFAULT_BENCHMARK_WEIGHTS);
        } catch (Exception e) {
            log.error("Benchmark verisi hesaplanırken hata oluştu. Fon: {}", fundCode, e);
            throw new StressTestException(StressTestErrorType.MODEL_INFERENCE_ERROR, e);
        }
    }

    public BigDecimal computeBenchmarkValue(BigDecimal totalValue,
                                            BigDecimal fundReturnPct,
                                            BigDecimal benchmarkReturnPct) {
        if (fundReturnPct == null || benchmarkReturnPct == null || totalValue == null) {
            return totalValue;
        }

        BigDecimal fundRate = fundReturnPct.movePointLeft(2);
        BigDecimal benchmarkRate = benchmarkReturnPct.movePointLeft(2);

        BigDecimal investedAmount = totalValue.divide(
                BigDecimal.ONE.add(fundRate), 6, RoundingMode.HALF_UP);

        return investedAmount.multiply(BigDecimal.ONE.add(benchmarkRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private PortfolioDataDto createPortfolioData(BigDecimal initialValue, Map<String, Float> weights) {
        return PortfolioDataDto.builder()
                .initialValue(initialValue)
                .assetWeights(weights)
                .build();
    }
}
