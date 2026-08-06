package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.exception.FundPeriodMetricNotFoundException;
import com.akademi.finsight.fund.service.FundDashboardService;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.fund.service.FundService;
import com.akademi.finsight.fund.service.FundStockAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundDashboardServiceImpl implements FundDashboardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int PERCENT_SCALE = 2;

    private final FundService fundService;
    private final FundPeriodMetricService fundPeriodMetricService;
    private final FundDistributionService fundDistributionService;
    private final FundStockAllocationService fundStockAllocationService;
    private final FundProperties fundProperties;

    @Override
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

        return new FundDashboardResponse(
                new FundDashboardResponse.FundInfo(fund.code(), fund.name(), reference.dataDate()),
                reference.totalValue(),
                reference.dailyReturn(),
                metrics.stream().map(this::toPeriodMetrics).toList(),
                toDistribution(fundCode),
                fundStockAllocationService.getBreakdownByFundCode(fundCode, null));
    }

    private int configuredOrder(FundPeriodMetricResponse metric) {
        int index = fundProperties.getPeriods().indexOf(metric.period());
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private FundDashboardResponse.PeriodMetrics toPeriodMetrics(FundPeriodMetricResponse metric) {
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
                metric.benchmarkDiffBps());
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
