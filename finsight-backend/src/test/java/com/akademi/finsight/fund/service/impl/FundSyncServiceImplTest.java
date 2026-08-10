package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.dto.sync.FundSyncSnapshot;
import com.akademi.finsight.fund.exception.FundSyncException;
import com.akademi.finsight.integration.infina.dto.response.fund.FundAssetDistributionResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundReturnResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FundSyncServiceImpl")
class FundSyncServiceImplTest {

    private static final String FUND_CODE = "TIE";
    private static final String FUND_NAME = "IS PORTFOY BIST 30 ENDEKSI";
    private static final LocalDate DATA_DATE = LocalDate.of(2026, Month.JULY, 31);
    private static final LocalDate P10D_BEGIN = DATA_DATE.minusDays(10);
    private static final LocalDate P30D_BEGIN = DATA_DATE.minusDays(30);
    private static final LocalDate P1D_BEGIN = DATA_DATE.minusDays(1);
    private static final String ALLOCATION_PERIOD = "2026-07";
    private static final String STOCK = "HİSSE SENEDİ";

    @Mock
    private InfinaService infinaService;

    @Mock
    private FundSyncPersister fundSyncPersister;

    @Captor
    private ArgumentCaptor<FundSyncSnapshot> snapshotCaptor;

    @Captor
    private ArgumentCaptor<String> periodsCaptor;

    private FundProperties fundProperties;
    private FundSyncServiceImpl service;

    @BeforeEach
    void setUp() {
        fundProperties = new FundProperties();
        fundProperties.setCode(FUND_CODE);
        fundProperties.setPeriods(List.of("P10D", "P30D"));

        service = new FundSyncServiceImpl(infinaService, fundSyncPersister, fundProperties);
    }

    private static FundReturnResponse periodReturn(String period,
                                                   String fundReturn,
                                                   LocalDate beginDate,
                                                   String benchmarkReturn) {
        return new FundReturnResponse(
                period,
                fundReturn == null ? null : new BigDecimal(fundReturn),
                beginDate,
                benchmarkReturn == null ? null : new BigDecimal(benchmarkReturn));
    }

    private static List<FundReturnResponse> defaultReturns() {
        return List.of(
                periodReturn("P1D", "0.15", P1D_BEGIN, null),
                periodReturn("P10D", "1.10", P10D_BEGIN, "0.90"),
                periodReturn("P30D", "5.67", P30D_BEGIN, "4.20"));
    }

    private static FundInfoResponse fundInfo(List<FundReturnResponse> periodReturns,
                                             List<FundAssetDistributionResponse> distribution) {
        return new FundInfoResponse(
                FUND_NAME, DATA_DATE, DATA_DATE, periodReturns,
                new BigDecimal("1699484991.5"), distribution, 1234);
    }

    private static FundPortfolioAllocationResponse allocation(String assetCode,
                                                              String ratio,
                                                              String period,
                                                              String assetType,
                                                              Long disclosureId) {
        return new FundPortfolioAllocationResponse(
                assetCode, ratio == null ? null : new BigDecimal(ratio), period, assetType, disclosureId);
    }

    private static List<FundPortfolioAllocationResponse> defaultAllocations() {
        return List.of(
                allocation("ASELS", "13.44", ALLOCATION_PERIOD, STOCK, 42L),
                allocation("THYAO", "9.10", ALLOCATION_PERIOD, STOCK, 42L));
    }

    private void stubFundInfo(FundInfoResponse response) {
        lenient().when(infinaService.getFundInfo(eq(FUND_CODE), isNull(), anyString())).thenReturn(response);
    }

    private void stubBeginSnapshot(LocalDate beginDate, String totalValue) {
        lenient().when(infinaService.getFundInfo(FUND_CODE, beginDate.toString(), null))
                .thenReturn(new FundInfoResponse(
                        FUND_NAME, beginDate, beginDate, List.of(),
                        totalValue == null ? null : new BigDecimal(totalValue), List.of(), null));
    }

    private void stubAllocations(List<FundPortfolioAllocationResponse> allocations) {
        lenient().when(infinaService.getFundPortfolioAllocation(FUND_CODE, null, null))
                .thenReturn(allocations);
    }

    private void stubHappyPath() {
        stubFundInfo(fundInfo(defaultReturns(), List.of(
                new FundAssetDistributionResponse("Hisse Senedi", new BigDecimal("45.5")),
                new FundAssetDistributionResponse("Borçlanma Araçları", new BigDecimal("30.0")))));
        stubBeginSnapshot(P10D_BEGIN, "1000000");
        stubBeginSnapshot(P30D_BEGIN, "900000");
        stubAllocations(defaultAllocations());
    }

    private FundSyncSnapshot sync() {
        service.sync();
        verify(fundSyncPersister).persist(snapshotCaptor.capture());
        return snapshotCaptor.getValue();
    }

    @Nested
    @DisplayName("Infina request")
    class InfinaRequest {

        @Test
        @DisplayName("should expand the requested periods from P1D up to the longest configured one")
        void shouldExpandRequestedPeriods() {
            stubHappyPath();

            service.sync();

            verify(infinaService).getFundInfo(eq(FUND_CODE), isNull(), periodsCaptor.capture());
            List<String> requested = List.of(periodsCaptor.getValue().split(","));

            assertTrue(requested.contains("P1D"));
            assertTrue(requested.contains("P30D"));
            assertTrue(requested.contains("P17D"), "gap days should be requested for the benchmark series");
            assertEquals(30, requested.size());
            assertEquals(requested.size(), requested.stream().distinct().count());
        }

        @Test
        @DisplayName("should ask for the latest data when no lag is configured")
        void shouldAskForLatestDataWithoutLag() {
            stubHappyPath();

            service.sync();

            verify(infinaService).getFundInfo(eq(FUND_CODE), isNull(), anyString());
        }

        @Test
        @DisplayName("should shift the request date back by the configured lag")
        void shouldApplyDataLag() {
            fundProperties.getSync().setDataLagDays(8);
            String expectedDate = LocalDate.now().minusDays(8).toString();

            lenient().when(infinaService.getFundInfo(eq(FUND_CODE), eq(expectedDate), anyString()))
                    .thenReturn(fundInfo(defaultReturns(), List.of()));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(defaultAllocations());

            service.sync();

            verify(infinaService).getFundInfo(eq(FUND_CODE), eq(expectedDate), anyString());
        }
    }

    @Nested
    @DisplayName("period metrics")
    class PeriodMetrics {

        @Test
        @DisplayName("should keep only the configured periods and drop the daily one")
        void shouldKeepOnlyConfiguredPeriods() {
            stubHappyPath();

            List<FundSyncSnapshot.PeriodMetric> metrics = sync().periodMetrics();

            assertEquals(List.of("P10D", "P30D"), metrics.stream().map(FundSyncSnapshot.PeriodMetric::period).toList());
        }

        @Test
        @DisplayName("should fetch the begin-date snapshot to carry the previous total value")
        void shouldFetchBeginDateSnapshot() {
            stubHappyPath();

            FundSyncSnapshot.PeriodMetric tenDays = sync().periodMetrics().getFirst();

            verify(infinaService).getFundInfo(FUND_CODE, P10D_BEGIN.toString(), null);
            assertEquals(P10D_BEGIN, tenDays.previousDate());
            assertEquals(new BigDecimal("1000000.0000"), tenDays.previousTotalValue());
        }

        @Test
        @DisplayName("should leave the previous total value null when the begin-date snapshot has none")
        void shouldTolerateMissingPreviousTotal() {
            stubFundInfo(fundInfo(defaultReturns(), List.of()));
            stubBeginSnapshot(P10D_BEGIN, null);
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(defaultAllocations());

            assertNull(sync().periodMetrics().getFirst().previousTotalValue());
        }

        @Test
        @DisplayName("should skip a configured period that has no begin date or no return")
        void shouldSkipIncompletePeriod() {
            stubFundInfo(fundInfo(List.of(
                    periodReturn("P1D", "0.15", P1D_BEGIN, null),
                    periodReturn("P10D", "1.10", null, "0.90"),
                    periodReturn("P30D", null, P30D_BEGIN, "4.20")), List.of()));
            stubAllocations(defaultAllocations());

            assertThrows(FundSyncException.class, () -> service.sync());
        }

        @Test
        @DisplayName("should fail when no configured period survives")
        void shouldFailWithoutUsablePeriod() {
            stubFundInfo(fundInfo(List.of(periodReturn("P1D", "0.15", P1D_BEGIN, null)), List.of()));
            stubAllocations(defaultAllocations());

            assertThrows(FundSyncException.class, () -> service.sync());
            verify(fundSyncPersister, never()).persist(any());
        }

        @Test
        @DisplayName("should scale returns to six decimals")
        void shouldScaleReturns() {
            stubHappyPath();

            FundSyncSnapshot.PeriodMetric tenDays = sync().periodMetrics().getFirst();

            assertEquals(new BigDecimal("1.100000"), tenDays.cumulativeReturn());
            assertEquals(new BigDecimal("0.900000"), tenDays.benchmarkReturn());
        }
    }

    @Nested
    @DisplayName("daily return")
    class DailyReturn {

        @Test
        @DisplayName("should take the P1D return and scale it")
        void shouldTakeDailyReturn() {
            stubHappyPath();

            assertEquals(new BigDecimal("0.150000"), sync().dailyReturn());
        }

        @Test
        @DisplayName("should fail when Infina returns no P1D entry")
        void shouldFailWithoutDailyReturn() {
            stubFundInfo(fundInfo(List.of(
                    periodReturn("P10D", "1.10", P10D_BEGIN, "0.90")), List.of()));
            stubAllocations(defaultAllocations());

            assertThrows(FundSyncException.class, () -> service.sync());
        }

        @Test
        @DisplayName("should fail when the P1D entry has no return value")
        void shouldFailWhenDailyReturnIsNull() {
            stubFundInfo(fundInfo(List.of(
                    periodReturn("P1D", null, P1D_BEGIN, null),
                    periodReturn("P10D", "1.10", P10D_BEGIN, "0.90")), List.of()));
            stubAllocations(defaultAllocations());

            assertThrows(FundSyncException.class, () -> service.sync());
        }
    }

    @Nested
    @DisplayName("benchmark series")
    class BenchmarkSeries {

        @Test
        @DisplayName("should build one ascending point per begin date and close the series at the data date")
        void shouldBuildAscendingSeries() {
            stubHappyPath();

            List<FundSyncSnapshot.BenchmarkPoint> points = sync().benchmarkPoints();

            assertEquals(List.of(P30D_BEGIN, P10D_BEGIN, P1D_BEGIN, DATA_DATE),
                    points.stream().map(FundSyncSnapshot.BenchmarkPoint::date).toList());
            assertEquals(new BigDecimal("5.670000"), points.getFirst().fundReturn());
            assertEquals(BigDecimal.ZERO, points.getLast().fundReturn());
            assertEquals(BigDecimal.ZERO, points.getLast().benchmarkReturn());
        }

        @Test
        @DisplayName("should leave the benchmark null when Infina does not report one")
        void shouldTolerateMissingBenchmark() {
            stubHappyPath();

            FundSyncSnapshot.BenchmarkPoint daily = sync().benchmarkPoints().stream()
                    .filter(point -> P1D_BEGIN.equals(point.date()))
                    .findFirst()
                    .orElseThrow();

            assertNull(daily.benchmarkReturn());
        }

        @Test
        @DisplayName("should keep the first entry when two periods share a begin date")
        void shouldKeepFirstEntryPerDate() {
            List<FundReturnResponse> returns = new ArrayList<>(defaultReturns());
            returns.add(periodReturn("P11D", "9.99", P10D_BEGIN, "9.99"));

            stubFundInfo(fundInfo(returns, List.of()));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(defaultAllocations());

            FundSyncSnapshot.BenchmarkPoint shared = sync().benchmarkPoints().stream()
                    .filter(point -> P10D_BEGIN.equals(point.date()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(new BigDecimal("1.100000"), shared.fundReturn());
        }

        @Test
        @DisplayName("should drop entries that are not before the data date")
        void shouldDropEntriesOnOrAfterDataDate() {
            List<FundReturnResponse> returns = new ArrayList<>(defaultReturns());
            returns.add(periodReturn("P0D", "0.00", DATA_DATE, "0.00"));
            returns.add(periodReturn("PX", "0.00", DATA_DATE.plusDays(1), "0.00"));

            stubFundInfo(fundInfo(returns, List.of()));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(defaultAllocations());

            List<LocalDate> dates = sync().benchmarkPoints().stream()
                    .map(FundSyncSnapshot.BenchmarkPoint::date)
                    .toList();

            assertEquals(List.of(P30D_BEGIN, P10D_BEGIN, P1D_BEGIN, DATA_DATE), dates);
        }

        @Test
        @DisplayName("should produce no series at all when no entry predates the data date")
        void shouldProduceEmptySeries() {
            stubFundInfo(fundInfo(List.of(
                    periodReturn("P1D", "0.15", null, null),
                    periodReturn("P10D", "1.10", DATA_DATE, "0.90")), List.of()));
            stubBeginSnapshot(DATA_DATE, "1000000");
            stubAllocations(defaultAllocations());

            assertTrue(sync().benchmarkPoints().isEmpty());
        }
    }

    @Nested
    @DisplayName("asset distribution")
    class AssetDistribution {

        @Test
        @DisplayName("should merge repeated categories and trim their labels")
        void shouldMergeRepeatedCategories() {
            stubFundInfo(fundInfo(defaultReturns(), List.of(
                    new FundAssetDistributionResponse(" Hisse Senedi ", new BigDecimal("20.5")),
                    new FundAssetDistributionResponse("Hisse Senedi", new BigDecimal("25.0")))));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(defaultAllocations());

            List<FundSyncSnapshot.Distribution> distributions = sync().distributions();

            assertEquals(1, distributions.size());
            assertEquals("Hisse Senedi", distributions.getFirst().category());
            assertEquals(new BigDecimal("45.500000"), distributions.getFirst().weight());
        }

        @Test
        @DisplayName("should skip entries without a label or a ratio")
        void shouldSkipUnusableEntries() {
            stubFundInfo(fundInfo(defaultReturns(), List.of(
                    new FundAssetDistributionResponse("  ", new BigDecimal("10.0")),
                    new FundAssetDistributionResponse(null, new BigDecimal("10.0")),
                    new FundAssetDistributionResponse("Nakit", null),
                    new FundAssetDistributionResponse("Hisse Senedi", new BigDecimal("45.5")))));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(defaultAllocations());

            List<FundSyncSnapshot.Distribution> distributions = sync().distributions();

            assertEquals(1, distributions.size());
            assertEquals("Hisse Senedi", distributions.getFirst().category());
        }
    }

    @Nested
    @DisplayName("stock breakdown")
    class StockBreakdown {

        @Test
        @DisplayName("should report the latest disclosure period")
        void shouldReportLatestPeriod() {
            stubFundInfo(fundInfo(defaultReturns(), List.of()));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(List.of(
                    allocation("ASELS", "5.00", "2026-05", STOCK, 40L),
                    allocation("ASELS", "13.44", ALLOCATION_PERIOD, STOCK, 42L)));

            FundSyncSnapshot snapshot = sync();

            assertEquals(ALLOCATION_PERIOD, snapshot.allocationPeriod());
            assertEquals(1, snapshot.stockAllocations().size());
            assertEquals(new BigDecimal("13.440000"), snapshot.stockAllocations().getFirst().weight());
        }

        @Test
        @DisplayName("should keep only the latest disclosure within that period")
        void shouldKeepLatestDisclosure() {
            stubFundInfo(fundInfo(defaultReturns(), List.of()));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(List.of(
                    allocation("ASELS", "5.00", ALLOCATION_PERIOD, STOCK, 41L),
                    allocation("ASELS", "13.44", ALLOCATION_PERIOD, STOCK, 42L),
                    allocation("THYAO", "9.10", ALLOCATION_PERIOD, STOCK, 42L)));

            List<FundSyncSnapshot.StockAllocation> stocks = sync().stockAllocations();

            assertEquals(2, stocks.size());
            assertEquals(new BigDecimal("13.440000"), stocks.getFirst().weight());
        }

        @Test
        @DisplayName("should keep only stock asset types")
        void shouldKeepOnlyStocks() {
            stubFundInfo(fundInfo(defaultReturns(), List.of()));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(List.of(
                    allocation("ASELS", "13.44", ALLOCATION_PERIOD, STOCK, 42L),
                    allocation("TRT120126T18", "20.00", ALLOCATION_PERIOD, "DEVLET TAHVİLİ", 42L),
                    allocation("TL", "5.00", ALLOCATION_PERIOD, null, 42L)));

            List<FundSyncSnapshot.StockAllocation> stocks = sync().stockAllocations();

            assertEquals(1, stocks.size());
            assertEquals("ASELS", stocks.getFirst().assetCode());
        }

        @Test
        @DisplayName("should merge repeated asset codes")
        void shouldMergeRepeatedAssetCodes() {
            stubFundInfo(fundInfo(defaultReturns(), List.of()));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(List.of(
                    allocation(" ASELS ", "3.44", ALLOCATION_PERIOD, STOCK, 42L),
                    allocation("ASELS", "10.00", ALLOCATION_PERIOD, STOCK, 42L)));

            List<FundSyncSnapshot.StockAllocation> stocks = sync().stockAllocations();

            assertEquals(1, stocks.size());
            assertEquals(new BigDecimal("13.440000"), stocks.getFirst().weight());
        }

        @Test
        @DisplayName("should sync without a breakdown when Infina reports no allocation period")
        void shouldSyncWithoutBreakdown() {
            stubFundInfo(fundInfo(defaultReturns(), List.of()));
            stubBeginSnapshot(P10D_BEGIN, "1000000");
            stubBeginSnapshot(P30D_BEGIN, "900000");
            stubAllocations(List.of());

            FundSyncSnapshot snapshot = sync();

            assertNull(snapshot.allocationPeriod());
            assertTrue(snapshot.stockAllocations().isEmpty());
        }
    }

    @Nested
    @DisplayName("required fields")
    class RequiredFields {

        @Test
        @DisplayName("should fail when Infina reports no fund date")
        void shouldFailWithoutFundDate() {
            lenient().when(infinaService.getFundInfo(eq(FUND_CODE), isNull(), anyString()))
                    .thenReturn(new FundInfoResponse(FUND_NAME, DATA_DATE, null, defaultReturns(),
                            new BigDecimal("1699484991.5"), List.of(), 1234));

            assertThrows(FundSyncException.class, () -> service.sync());
            verify(fundSyncPersister, never()).persist(any());
        }

        @Test
        @DisplayName("should fail when Infina reports no total market price")
        void shouldFailWithoutTotalValue() {
            lenient().when(infinaService.getFundInfo(eq(FUND_CODE), isNull(), anyString()))
                    .thenReturn(new FundInfoResponse(FUND_NAME, DATA_DATE, DATA_DATE, defaultReturns(),
                            null, List.of(), 1234));

            assertThrows(FundSyncException.class, () -> service.sync());
        }

        @Test
        @DisplayName("should carry the fund identity and the scaled total value into the snapshot")
        void shouldCarryFundIdentity() {
            stubHappyPath();

            FundSyncSnapshot snapshot = sync();

            assertEquals(FUND_CODE, snapshot.fundCode());
            assertEquals(FUND_NAME, snapshot.fundName());
            assertEquals(DATA_DATE, snapshot.dataDate());
            assertEquals(new BigDecimal("1699484991.5000"), snapshot.totalValue());
        }
    }
}
