package com.akademi.finsight.fund.performancecomparison.service.impl;

import com.akademi.finsight.fund.decision.entity.AssetCategory;
import com.akademi.finsight.fund.decision.entity.ManualScenario;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.entity.FundBenchmarkPoint;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioCurve;
import com.akademi.finsight.fund.repository.FundBenchmarkPointRepository;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.fund.stockprice.entity.StockPriceHistory;
import com.akademi.finsight.fund.stockprice.service.StockPriceService;
import com.akademi.finsight.integration.infina.dto.response.fund.FundDailyReturnResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioSimulationCalculationServiceImpl Tests")
class PortfolioSimulationCalculationServiceImplTest {

    @Mock
    private FundPeriodMetricService fundPeriodMetricService;

    @Mock
    private FundDistributionService fundDistributionService;

    @Mock
    private FundBenchmarkPointRepository fundBenchmarkPointRepository;

    @Mock
    private StockPriceService stockPriceService;

    @Mock
    private InfinaService infinaService;

    @InjectMocks
    private PortfolioSimulationCalculationServiceImpl simulationCalculationService;

    private static final String FUND_CODE = "TIE";
    private static final int ANALYSIS_WINDOW = 30;
    private static final LocalDate DATA_DATE = LocalDate.of(2026, 1, 31);

    private FundPeriodMetricResponse metric;

    @BeforeEach
    void setUp() {
        metric = new FundPeriodMetricResponse(
                null, null, DATA_DATE, "P30D",
                BigDecimal.valueOf(1_000_000), null, null, null,
                BigDecimal.valueOf(5), BigDecimal.valueOf(4), null, null, null);
    }

    @Nested
    @DisplayName("calculateSimulation")
    class CalculateSimulation {

        @Test
        @DisplayName("should return null when fund has no STOCK category weight")
        void shouldReturnNullWhenNoStockWeight() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(infinaService.getFundDailyReturn(eq(FUND_CODE), anyString()))
                    .thenReturn(new FundDailyReturnResponse(FUND_CODE, List.of(BigDecimal.valueOf(0.01))));
            when(fundDistributionService.getLatestByFundCode(FUND_CODE)).thenReturn(List.of());

            PortfolioCurve result = simulationCalculationService.calculateSimulation(
                    FUND_CODE, ANALYSIS_WINDOW, Map.of(AssetCategory.STOCK, BigDecimal.valueOf(50)), Map.of());

            assertNull(result);
            verifyNoInteractions(fundBenchmarkPointRepository, stockPriceService);
        }

        @Test
        @DisplayName("should use category-only fallback formula when no top-N stock weights given")
        void shouldUseCategoryFallbackWhenNoStockWeights() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(infinaService.getFundDailyReturn(eq(FUND_CODE), anyString()))
                    .thenReturn(new FundDailyReturnResponse(FUND_CODE, List.of(
                            BigDecimal.valueOf(0.01), BigDecimal.valueOf(-0.005))));
            when(fundDistributionService.getLatestByFundCode(FUND_CODE)).thenReturn(List.of(
                    new FundDistributionResponse(null, null, "Hisse Senedi", BigDecimal.valueOf(60), null, null)
            ));

            List<LocalDate> dates = List.of(DATA_DATE.minusDays(1), DATA_DATE);
            List<FundBenchmarkPoint> benchmarkWindow = new ArrayList<>();
            for (LocalDate d : dates) {
                benchmarkWindow.add(FundBenchmarkPoint.builder().dataDate(d).fundReturn(BigDecimal.ONE).build());
            }
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(benchmarkWindow);

            PortfolioCurve result = simulationCalculationService.calculateSimulation(
                    FUND_CODE, ANALYSIS_WINDOW,
                    Map.of(AssetCategory.STOCK, BigDecimal.valueOf(60)), Map.of());

            assertNotNull(result);
            assertEquals(2, result.points().size());
            verifyNoInteractions(stockPriceService);
        }

        @Test
        @DisplayName("should use real per-stock price data when top-N stock weights are given")
        void shouldUsePerStockPricesWhenTopNGiven() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(infinaService.getFundDailyReturn(eq(FUND_CODE), anyString()))
                    .thenReturn(new FundDailyReturnResponse(FUND_CODE, List.of(
                            BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.02))));
            when(fundDistributionService.getLatestByFundCode(FUND_CODE)).thenReturn(List.of(
                    new FundDistributionResponse(null, null, "Hisse Senedi", BigDecimal.valueOf(60), null, null)
            ));

            List<LocalDate> dates = List.of(DATA_DATE.minusDays(1), DATA_DATE);
            List<FundBenchmarkPoint> benchmarkWindow = new ArrayList<>();
            for (LocalDate d : dates) {
                benchmarkWindow.add(FundBenchmarkPoint.builder().dataDate(d).fundReturn(BigDecimal.ONE).build());
            }
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(benchmarkWindow);

            when(stockPriceService.getWindow(eq("THYAO"), any(), eq(DATA_DATE))).thenReturn(List.of(
                    StockPriceHistory.builder().assetCode("THYAO").dataDate(dates.get(0)).closePrice(BigDecimal.valueOf(100)).build(),
                    StockPriceHistory.builder().assetCode("THYAO").dataDate(dates.get(1)).closePrice(BigDecimal.valueOf(103)).build()
            ));

            PortfolioCurve result = simulationCalculationService.calculateSimulation(
                    FUND_CODE, ANALYSIS_WINDOW,
                    Map.of(AssetCategory.STOCK, BigDecimal.valueOf(60)),
                    Map.of("THYAO", BigDecimal.valueOf(30)));

            assertNotNull(result);
            verify(stockPriceService).getWindow(eq("THYAO"), any(), eq(DATA_DATE));
        }

        @Test
        @DisplayName("should exclude 'Others' pseudo asset code and zero-weight stocks from top-N lookup")
        void shouldExcludeOthersAndZeroWeights() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(infinaService.getFundDailyReturn(eq(FUND_CODE), anyString()))
                    .thenReturn(new FundDailyReturnResponse(FUND_CODE, List.of(BigDecimal.valueOf(0.01))));
            when(fundDistributionService.getLatestByFundCode(FUND_CODE)).thenReturn(List.of(
                    new FundDistributionResponse(null, null, "Hisse Senedi", BigDecimal.valueOf(60), null, null)
            ));
            List<FundBenchmarkPoint> singleDayWindow = new ArrayList<>();
            singleDayWindow.add(FundBenchmarkPoint.builder().dataDate(DATA_DATE).fundReturn(BigDecimal.ONE).build());
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(singleDayWindow);

            Map<String, BigDecimal> stockWeights = new LinkedHashMap<>();
            stockWeights.put("Others", BigDecimal.valueOf(40));
            stockWeights.put("GARAN", BigDecimal.ZERO);

            simulationCalculationService.calculateSimulation(
                    FUND_CODE, ANALYSIS_WINDOW,
                    Map.of(AssetCategory.STOCK, BigDecimal.valueOf(60)), stockWeights);

            verifyNoInteractions(stockPriceService);
        }
    }

    @Nested
    @DisplayName("attachSnapshot")
    class AttachSnapshot {

        @Test
        @DisplayName("should populate holder metrics and benchmark diff when simulation succeeds")
        void shouldPopulateMetricsOnSuccess() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(infinaService.getFundDailyReturn(eq(FUND_CODE), anyString()))
                    .thenReturn(new FundDailyReturnResponse(FUND_CODE, List.of(BigDecimal.valueOf(0.01))));
            when(fundDistributionService.getLatestByFundCode(FUND_CODE)).thenReturn(List.of(
                    new FundDistributionResponse(null, null, "Hisse Senedi", BigDecimal.valueOf(60), null, null)
            ));
            List<FundBenchmarkPoint> singleDayWindow = new ArrayList<>();
            singleDayWindow.add(FundBenchmarkPoint.builder().dataDate(DATA_DATE).fundReturn(BigDecimal.ONE).build());
            when(fundBenchmarkPointRepository.findWindowByFundCode(eq(FUND_CODE), any(), eq(DATA_DATE)))
                    .thenReturn(singleDayWindow);

            ManualScenario holder = ManualScenario.builder().build();

            simulationCalculationService.attachSnapshot(
                    holder, FUND_CODE, ANALYSIS_WINDOW, Map.of(AssetCategory.STOCK, BigDecimal.valueOf(60)), Map.of());

            assertNotNull(holder.getMetrics());
            assertNotNull(holder.getMetrics().getSimulatedPortfolioValue());
            assertNotNull(holder.getMetrics().getTotalReturnPct());
            assertEquals(ANALYSIS_WINDOW, holder.getMetrics().getAnalysisWindowDays());
            assertNotNull(holder.getMetrics().getBenchmarkDiffPct());
        }

        @Test
        @DisplayName("should leave holder metrics untouched when simulation returns null (no STOCK weight)")
        void shouldSkipWhenSimulationReturnsNull() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D")).thenReturn(metric);
            when(infinaService.getFundDailyReturn(eq(FUND_CODE), anyString()))
                    .thenReturn(new FundDailyReturnResponse(FUND_CODE, List.of(BigDecimal.valueOf(0.01))));
            when(fundDistributionService.getLatestByFundCode(FUND_CODE)).thenReturn(List.of());

            ManualScenario holder = ManualScenario.builder().build();

            simulationCalculationService.attachSnapshot(
                    holder, FUND_CODE, ANALYSIS_WINDOW, Map.of(AssetCategory.STOCK, BigDecimal.valueOf(60)), Map.of());

            assertNotNull(holder.getMetrics());
            assertNull(holder.getMetrics().getSimulatedPortfolioValue());
            assertNull(holder.getMetrics().getTotalReturnPct());
        }

        @Test
        @DisplayName("should swallow exceptions from the simulation calculation and leave holder untouched")
        void shouldSwallowExceptions() {
            when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(FUND_CODE, "P30D"))
                    .thenThrow(new RuntimeException("boom"));

            ManualScenario holder = ManualScenario.builder().build();

            simulationCalculationService.attachSnapshot(
                    holder, FUND_CODE, ANALYSIS_WINDOW, Map.of(AssetCategory.STOCK, BigDecimal.valueOf(60)), Map.of());

            assertNotNull(holder.getMetrics());
            assertNull(holder.getMetrics().getSimulatedPortfolioValue());
        }
    }
}
