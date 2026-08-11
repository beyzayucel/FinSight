package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.constant.CacheNames;
import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.entity.FundBenchmarkPoint;
import com.akademi.finsight.fund.exception.FundPeriodMetricNotFoundException;
import com.akademi.finsight.fund.repository.FundBenchmarkPointRepository;
import com.akademi.finsight.fund.service.FundDashboardService;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.fund.service.FundService;
import com.akademi.finsight.fund.service.FundStockAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundDashboardServiceImpl implements FundDashboardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int PERCENT_SCALE = 2;
    private static final int INDEX_SCALE = 6;

    private final FundService fundService;
    private final FundPeriodMetricService fundPeriodMetricService;
    private final FundBenchmarkPointRepository fundBenchmarkPointRepository;
    private final FundDistributionService fundDistributionService;
    private final FundStockAllocationService fundStockAllocationService;
    private final FundProperties fundProperties;

    @Override
    @Cacheable(cacheNames = CacheNames.FUND_DASHBOARD, key = "#fundCode")
    public FundDashboardResponse getDashboard(String fundCode) {
        FundResponse fund = fundService.getByCode(fundCode);

        List<FundPeriodMetricResponse> metrics =
                fundPeriodMetricService.getLatestByFundCode(fundCode).stream()
                        .sorted(Comparator.comparingInt(this::configuredOrder))
                        .toList();

        if (metrics.isEmpty()) {
            throw new FundPeriodMetricNotFoundException();
        }

        FundPeriodMetricResponse reference = metrics.getFirst();

        List<FundBenchmarkPoint> benchmarkPoints = loadBenchmarkPoints(fundCode, metrics, reference.dataDate());

        return new FundDashboardResponse(
                new FundDashboardResponse.FundInfo(fund.code(), fund.name(), reference.dataDate()),
                reference.totalValue(),
                reference.dailyReturn(),
                metrics.stream().map(metric -> toPeriodMetrics(metric, benchmarkPoints)).toList(),
                toDistribution(fundCode),
                fundStockAllocationService.getBreakdownByFundCode(fundCode, null));
    }

    private List<FundBenchmarkPoint> loadBenchmarkPoints(String fundCode,
                                                         List<FundPeriodMetricResponse> metrics,
                                                         LocalDate dataDate) {
        return metrics.stream()
                .map(FundPeriodMetricResponse::previousDate)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .map(earliest -> fundBenchmarkPointRepository
                        .findWindowByFundCode(fundCode, earliest, dataDate))
                .orElseGet(List::of);
    }

    private int configuredOrder(FundPeriodMetricResponse metric) {
        int index = fundProperties.getPeriods().indexOf(metric.period());
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private FundDashboardResponse.PeriodMetrics toPeriodMetrics(FundPeriodMetricResponse metric,
                                                                List<FundBenchmarkPoint> benchmarkPoints) {
        BigDecimal previous = metric.previousTotalValue();

        BigDecimal change = previous == null ? null : metric.totalValue().subtract(previous);
        BigDecimal changePercent = (previous == null || previous.signum() == 0)
                ? null
                : change.divide(previous, MathContext.DECIMAL64)
                        .multiply(HUNDRED)
                        .setScale(PERCENT_SCALE, RoundingMode.HALF_UP);

        Integer days = metric.previousDate() == null
                ? null
                : (int) ChronoUnit.DAYS.between(metric.previousDate(), metric.dataDate());

        return new FundDashboardResponse.PeriodMetrics(
                metric.period(),
                previous,
                metric.previousDate(),
                days,
                change,
                changePercent,
                metric.cumulativeReturn(),
                metric.benchmarkReturn(),
                metric.benchmarkDiffBps(),
                toSeries(metric, benchmarkPoints));
    }

    private List<FundDashboardResponse.SeriesPoint> toSeries(FundPeriodMetricResponse metric,
                                                             List<FundBenchmarkPoint> benchmarkPoints) {
        LocalDate periodStart = metric.previousDate();
        if (periodStart == null) {
            return List.of();
        }

        List<FundBenchmarkPoint> periodPoints = benchmarkPoints.stream()
                .filter(point -> !point.getDataDate().isBefore(periodStart)
                        && !point.getDataDate().isAfter(metric.dataDate()))
                .toList();

        if (periodPoints.isEmpty()) {
            return List.of();
        }

        FundBenchmarkPoint base = periodPoints.getFirst();

        return periodPoints.stream()
                .map(point -> new FundDashboardResponse.SeriesPoint(
                        point.getDataDate(),
                        rebase(base.getFundReturn(), point.getFundReturn()),
                        rebase(base.getBenchmarkReturn(), point.getBenchmarkReturn())))
                .toList();
    }

    private static BigDecimal rebase(BigDecimal baseCumulative, BigDecimal cumulative) {
        if (baseCumulative == null || cumulative == null) {
            return null;
        }

        BigDecimal factor = HUNDRED.add(cumulative);
        if (factor.signum() == 0) {
            return null;
        }

        return HUNDRED.add(baseCumulative)
                .multiply(HUNDRED)
                .divide(factor, MathContext.DECIMAL64)
                .setScale(INDEX_SCALE, RoundingMode.HALF_UP);
    }

    private List<FundDashboardResponse.CategoryWeight> toDistribution(String fundCode) {
        List<FundDistributionResponse> distributions =
                fundDistributionService.getLatestByFundCode(fundCode);

        return distributions.stream()
                .map(distribution -> new FundDashboardResponse.CategoryWeight(
                        distribution.category(), distribution.weight()))
                .toList();
    }
}
