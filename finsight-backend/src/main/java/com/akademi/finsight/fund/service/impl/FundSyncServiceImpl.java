package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.dto.response.FundSyncResponse;
import com.akademi.finsight.fund.dto.sync.FundSyncSnapshot;
import com.akademi.finsight.fund.exception.FundSyncException;
import com.akademi.finsight.fund.service.FundSyncService;
import com.akademi.finsight.integration.infina.dto.response.fund.FundAssetDistributionResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundReturnResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundSyncServiceImpl implements FundSyncService {

    private static final String DAILY_RETURN_PERIOD = "P1D";
    private static final String STOCK_ASSET_TYPE_PREFIX = "HİSSE";
    private static final String DAY_PERIOD = "P\\d+D";
    private static final int TOTAL_VALUE_SCALE = 4;
    private static final int RETURN_SCALE = 6;
    private static final int WEIGHT_SCALE = 6;

    private final InfinaService infinaService;
    private final FundSyncPersister fundSyncPersister;
    private final FundProperties fundProperties;

    @Override
    public FundSyncResponse sync() {
        String fundCode = fundProperties.getCode();

        String periods = buildRequestedPeriods();
        String requestDate = resolveRequestDate();
        log.info("Fund sync started: fundCode={}, requestDate={}, periods={}",
                fundCode, requestDate == null ? "LATEST" : requestDate, periods);

        FundInfoResponse fundInfo = infinaService.getFundInfo(fundCode, requestDate, periods);

        LocalDate dataDate = required(fundInfo.fundDate(), fundCode, "fundDate");
        BigDecimal totalValue = scaled(required(fundInfo.totalMarketPrice(), fundCode, "totalMarketPrice"),
                TOTAL_VALUE_SCALE);

        StockBreakdown stockBreakdown = resolveStockBreakdown(fundCode);

        FundSyncSnapshot snapshot = new FundSyncSnapshot(
                fundCode,
                fundInfo.name(),
                dataDate,
                totalValue,
                resolveDailyReturn(fundInfo, fundCode),
                resolvePeriodMetrics(fundCode, fundInfo.periodReturns()),
                resolveBenchmarkPoints(dataDate, fundInfo.periodReturns()),
                resolveDistributions(fundInfo.assetDistribution()),
                stockBreakdown.period(),
                stockBreakdown.allocations());

        return fundSyncPersister.persist(snapshot);
    }

    private String resolveRequestDate() {
        int dataLagDays = fundProperties.getSync().getDataLagDays();
        return dataLagDays > 0 ? LocalDate.now().minusDays(dataLagDays).toString() : null;
    }

    private String buildRequestedPeriods() {
        Set<String> requestedPeriods = new LinkedHashSet<>();
        requestedPeriods.add(DAILY_RETURN_PERIOD);
        requestedPeriods.addAll(fundProperties.getPeriods());

        int longestPeriod = fundProperties.getPeriods().stream()
                .filter(period -> period.matches(DAY_PERIOD))
                .mapToInt(period -> Integer.parseInt(period.substring(1, period.length() - 1)))
                .max()
                .orElse(0);

        for (int day = 1; day <= longestPeriod; day++) {
            requestedPeriods.add("P" + day + "D");
        }

        return String.join(",", requestedPeriods);
    }

    private List<FundSyncSnapshot.PeriodMetric> resolvePeriodMetrics(String fundCode,
                                                                    List<FundReturnResponse> periodReturns) {
        Set<String> configuredPeriods = new LinkedHashSet<>(fundProperties.getPeriods());
        List<FundSyncSnapshot.PeriodMetric> metrics = new ArrayList<>();

        List<FundReturnResponse> configuredReturns = periodReturns.stream()
                .filter(periodReturn -> !DAILY_RETURN_PERIOD.equals(periodReturn.period()))
                .filter(periodReturn -> configuredPeriods.contains(periodReturn.period()))
                .toList();

        for (FundReturnResponse periodReturn : configuredReturns) {
            if (periodReturn.beginDate() == null || periodReturn.fundReturn() == null) {
                log.warn("Skipping period with incomplete data: fundCode={}, period={}",
                        fundCode, periodReturn.period());
                continue;
            }

            LocalDate beginDate = periodReturn.beginDate();
            FundInfoResponse beginInfo = infinaService.getFundInfo(fundCode, beginDate.toString(), null);

            metrics.add(new FundSyncSnapshot.PeriodMetric(
                    periodReturn.period(),
                    beginDate,
                    beginInfo.totalMarketPrice() == null
                            ? null
                            : scaled(beginInfo.totalMarketPrice(), TOTAL_VALUE_SCALE),
                    scaled(periodReturn.fundReturn(), RETURN_SCALE),
                    periodReturn.benchmarkReturn() == null
                            ? null
                            : scaled(periodReturn.benchmarkReturn(), RETURN_SCALE)));
        }

        if (metrics.isEmpty()) {
            log.warn("Infina returned no usable period metric: event=FUND_SYNC_FAILED, fundCode={}", fundCode);
            throw new FundSyncException();
        }

        return metrics;
    }

    private List<FundSyncSnapshot.BenchmarkPoint> resolveBenchmarkPoints(LocalDate dataDate,
                                                                         List<FundReturnResponse> periodReturns) {
        TreeMap<LocalDate, FundSyncSnapshot.BenchmarkPoint> byDate = new TreeMap<>();

        for (FundReturnResponse periodReturn : periodReturns) {
            if (periodReturn.beginDate() == null || periodReturn.fundReturn() == null
                    || !periodReturn.beginDate().isBefore(dataDate)) {
                continue;
            }
            byDate.putIfAbsent(periodReturn.beginDate(), new FundSyncSnapshot.BenchmarkPoint(
                    periodReturn.beginDate(),
                    scaled(periodReturn.fundReturn(), RETURN_SCALE),
                    periodReturn.benchmarkReturn() == null
                            ? null
                            : scaled(periodReturn.benchmarkReturn(), RETURN_SCALE)));
        }

        if (byDate.isEmpty()) {
            return List.of();
        }

        byDate.put(dataDate, new FundSyncSnapshot.BenchmarkPoint(dataDate, BigDecimal.ZERO, BigDecimal.ZERO));

        return List.copyOf(byDate.values());
    }

    private BigDecimal resolveDailyReturn(FundInfoResponse fundInfo, String fundCode) {
        return fundInfo.periodReturns().stream()
                .filter(periodReturn -> DAILY_RETURN_PERIOD.equals(periodReturn.period())
                        && periodReturn.fundReturn() != null)
                .map(FundReturnResponse::fundReturn)
                .findFirst()
                .map(fundReturn -> scaled(fundReturn, RETURN_SCALE))
                .orElseThrow(() -> {
                    log.warn("Infina response has no {} return: event=FUND_SYNC_FAILED, fundCode={}",
                            DAILY_RETURN_PERIOD, fundCode);
                    return new FundSyncException();
                });
    }

    private List<FundSyncSnapshot.Distribution> resolveDistributions(
            List<FundAssetDistributionResponse> assetDistribution) {

        Map<String, BigDecimal> ratiosByCategory = new LinkedHashMap<>();
        assetDistribution.stream()
                .filter(distribution -> hasText(distribution.shortDesc()) && distribution.ratio() != null)
                .forEach(distribution ->
                        ratiosByCategory.merge(distribution.shortDesc().trim(), distribution.ratio(), BigDecimal::add));

        return ratiosByCategory.entrySet().stream()
                .map(entry -> new FundSyncSnapshot.Distribution(entry.getKey(), scaled(entry.getValue(), WEIGHT_SCALE)))
                .toList();
    }

    private StockBreakdown resolveStockBreakdown(String fundCode) {
        List<FundPortfolioAllocationResponse> allocations =
                infinaService.getFundPortfolioAllocation(fundCode, null, null);

        Optional<String> latestPeriod = allocations.stream()
                .map(FundPortfolioAllocationResponse::period)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());

        if (latestPeriod.isEmpty()) {
            log.warn("Infina returned no portfolio allocation period: fundCode={}", fundCode);
            return new StockBreakdown(null, List.of());
        }

        String period = latestPeriod.get();
        List<FundPortfolioAllocationResponse> periodAllocations = allocations.stream()
                .filter(allocation -> period.equals(allocation.period()))
                .toList();

        Optional<Long> latestDisclosureId = periodAllocations.stream()
                .map(FundPortfolioAllocationResponse::disclosureId)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());

        Map<String, BigDecimal> ratiosByAssetCode = new LinkedHashMap<>();
        periodAllocations.stream()
                .filter(allocation -> latestDisclosureId
                        .map(disclosureId -> disclosureId.equals(allocation.disclosureId()))
                        .orElse(true))
                .filter(FundSyncServiceImpl::isStock)
                .filter(allocation -> hasText(allocation.assetCode()) && allocation.ratio() != null)
                .forEach(allocation ->
                        ratiosByAssetCode.merge(allocation.assetCode().trim(), allocation.ratio(), BigDecimal::add));

        List<FundSyncSnapshot.StockAllocation> stockAllocations = ratiosByAssetCode.entrySet().stream()
                .map(entry -> new FundSyncSnapshot.StockAllocation(entry.getKey(), scaled(entry.getValue(), WEIGHT_SCALE)))
                .toList();

        if (stockAllocations.isEmpty()) {
            log.warn("Infina returned no stock allocation: fundCode={}, period={}", fundCode, period);
        }

        return new StockBreakdown(period, stockAllocations);
    }

    private static boolean isStock(FundPortfolioAllocationResponse allocation) {
        return allocation.assetType() != null && allocation.assetType().startsWith(STOCK_ASSET_TYPE_PREFIX);
    }

    private static BigDecimal scaled(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> T required(T value, String fundCode, String field) {
        if (value == null) {
            log.warn("Infina response is missing a required field: event=FUND_SYNC_FAILED, fundCode={}, field={}",
                    fundCode, field);
            throw new FundSyncException();
        }
        return value;
    }

    private record StockBreakdown(String period, List<FundSyncSnapshot.StockAllocation> allocations) {}
}
