package com.akademi.finsight.fund.performancecomparison.service.impl;

import com.akademi.finsight.fund.decision.entity.AssetCategory;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.entity.FundBenchmarkPoint;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioCurve;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioMetrics;
import com.akademi.finsight.fund.performancecomparison.dto.response.ScenarioSource;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.performancecomparison.service.impl.ScenarioResolver.ResolvedScenario;
import com.akademi.finsight.fund.repository.FundBenchmarkPointRepository;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceComparisonServiceImpl Tests")
class PerformanceComparisonServiceImplTest {

    @Mock
    private FundPeriodMetricService fundPeriodMetricService;

    @Mock
    private FundBenchmarkPointRepository fundBenchmarkPointRepository;

    @Mock
    private PortfolioSimulationCalculationService simulationCalculationService;

    @Mock
    private ScenarioResolver scenarioResolver;

    @InjectMocks
    private PerformanceComparisonServiceImpl performanceComparisonService;

    private static final String EMAIL = "user@test.com";
    private static final String FUND_CODE = "TIE";
    private static final int ANALYSIS_WINDOW = 30;
    private static final LocalDate DATA_DATE = LocalDate.of(2026, 1, 31);

    private FundPeriodMetricResponse metric;
    private List<FundBenchmarkPoint> benchmarkPoints;

    @BeforeEach
    void setUp() {
        metric = new FundPeriodMetricResponse(
                null, null, DATA_DATE, "P30D",
                BigDecimal.valueOf(1_056_679), null, null, null,
                BigDecimal.valueOf(5.67), BigDecimal.valueOf(4.20), null, null, null);

        benchmarkPoints = List.of(
                point(DATA_DATE.minusDays(1), BigDecimal.valueOf(2.0), BigDecimal.valueOf(1.5)),
                point(DATA_DATE, BigDecimal.valueOf(5.67), BigDecimal.valueOf(4.20))
        );
    }

    private FundBenchmarkPoint point(LocalDate date, BigDecimal fundReturn, BigDecimal benchmarkReturn) {
        return FundBenchmarkPoint.builder()
                .dataDate(date)
                .fundReturn(fundReturn)
                .benchmarkReturn(benchmarkReturn)
                .build();
    }

    @Nested
    @DisplayName("compare")
    class Compare {

        @Test
        @DisplayName("should build current and benchmark portfolios and skip simulation when no scenario resolved")
        void shouldBuildCurrentAndBenchmarkWithoutSimulation() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(benchmarkPoints);
            when(scenarioResolver.resolve(EMAIL, FUND_CODE)).thenReturn(Optional.empty());

            PerformanceComparisonResponse response =
                    performanceComparisonService.compare(EMAIL, FUND_CODE, ANALYSIS_WINDOW);

            assertNotNull(response.currentPortfolio());
            assertNotNull(response.benchmarkPortfolio());
            assertNull(response.simulationPortfolio());
            assertNull(response.scenarioSource());
            assertEquals(ANALYSIS_WINDOW, response.analysisWindow());

            verifyNoInteractions(simulationCalculationService);
        }

        @Test
        @DisplayName("should include simulation portfolio and scenario source when a scenario is resolved")
        void shouldIncludeSimulationWhenScenarioResolved() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(benchmarkPoints);

            Map<AssetCategory, BigDecimal> weights = Map.of(AssetCategory.STOCK, BigDecimal.valueOf(60));
            Map<String, BigDecimal> stockWeights = Map.of("THYAO", BigDecimal.valueOf(30));
            ResolvedScenario scenario = new ResolvedScenario(weights, stockWeights, ScenarioSource.MANUAL);
            when(scenarioResolver.resolve(EMAIL, FUND_CODE)).thenReturn(Optional.of(scenario));

            PortfolioCurve simulationCurve = new PortfolioCurve(List.of(),
                    new PortfolioMetrics(BigDecimal.valueOf(1_100_000), BigDecimal.valueOf(10),
                            BigDecimal.valueOf(-2), BigDecimal.valueOf(1)));
            when(simulationCalculationService.calculateSimulation(FUND_CODE, ANALYSIS_WINDOW, weights, stockWeights))
                    .thenReturn(simulationCurve);

            PerformanceComparisonResponse response =
                    performanceComparisonService.compare(EMAIL, FUND_CODE, ANALYSIS_WINDOW);

            assertNotNull(response.simulationPortfolio());
            assertEquals(simulationCurve, response.simulationPortfolio());
            assertEquals(ScenarioSource.MANUAL, response.scenarioSource());
        }

        @Test
        @DisplayName("should return null current/benchmark portfolios when benchmark window is empty")
        void shouldReturnNullPortfoliosWhenWindowEmpty() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(List.of());
            when(scenarioResolver.resolve(EMAIL, FUND_CODE)).thenReturn(Optional.empty());

            PerformanceComparisonResponse response =
                    performanceComparisonService.compare(EMAIL, FUND_CODE, ANALYSIS_WINDOW);

            assertNull(response.currentPortfolio());
            assertNull(response.benchmarkPortfolio());
        }

        @Test
        @DisplayName("should compute benchmark current value from invested-amount formula, not fund's own value")
        void shouldComputeBenchmarkValueViaInvestedAmountFormula() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(benchmarkPoints);
            when(scenarioResolver.resolve(EMAIL, FUND_CODE)).thenReturn(Optional.empty());

            PerformanceComparisonResponse response =
                    performanceComparisonService.compare(EMAIL, FUND_CODE, ANALYSIS_WINDOW);

            // investedAmount = 1.056.679 / 1.0567 = 999.980,126810
            // benchmarkValue = 999.980,126810 * 1.0420 = 1.041.979,29
            BigDecimal benchmarkCurrentValue = response.benchmarkPortfolio().metrics().currentValue();
            assertEquals(0, benchmarkCurrentValue.compareTo(BigDecimal.valueOf(1_041_979.29)));
        }

        @Test
        @DisplayName("should not call simulation calculation service when scenario resolver throws nothing but returns empty")
        void shouldSkipSimulationServiceCallWhenNoScenario() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(benchmarkPoints);
            when(scenarioResolver.resolve(EMAIL, FUND_CODE)).thenReturn(Optional.empty());

            performanceComparisonService.compare(EMAIL, FUND_CODE, ANALYSIS_WINDOW);

            verify(simulationCalculationService, never())
                    .calculateSimulation(anyString(), anyInt(), any(), any());
        }
    }
}
