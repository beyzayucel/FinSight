package com.akademi.finsight.fund.performancecomparison.service.impl;

import com.akademi.finsight.fund.converter.AssetCategoryConverter;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.entity.MetricsHolder;
import com.akademi.finsight.fund.entity.PerformanceMetrics;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.CurvePoint;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioCurve;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioMetrics;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.performancecomparison.util.PortfolioCalculationUtil;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.integration.infina.dto.response.fund.FundDailyReturnResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioSimulationCalculationServiceImpl implements PortfolioSimulationCalculationService {

    private static final String PERIOD_PREFIX = "P";
    private static final String PERIOD_SUFFIX = "D";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FundPeriodMetricService fundPeriodMetricService;
    private final FundDistributionService fundDistributionService;
    private final InfinaService infinaService;

    @Override
    public PortfolioCurve calculateSimulation(String fundCode, int analysisWindow,
                                              Map<AssetCategory, BigDecimal> simulationWeights) {
        String period = PERIOD_PREFIX + analysisWindow + PERIOD_SUFFIX;
        FundPeriodMetricResponse metric = fundPeriodMetricService.getLatestByFundCodeAndPeriod(fundCode, period);

        List<BigDecimal> fundDailyYields = fetchDailyYields(fundCode, analysisWindow, metric.dataDate());

        // TODO: Infina kategori bazlı getiri API'si gelince stockWeight çözümlemesi gereksiz kalacak.
        Optional<BigDecimal> stockWeightOpt = resolveCurrentStockWeight(fundCode);
        if (stockWeightOpt.isEmpty()) {
            log.warn("No STOCK weight found for fund {}. Cannot compute simulation.", fundCode);
            return null;
        }

        List<BigDecimal> simulationDailyYields = PortfolioCalculationUtil
                .deriveSimulationDailyReturns(fundDailyYields, stockWeightOpt.get(), simulationWeights);

        List<CurvePoint> curve = PortfolioCalculationUtil.buildCumulativeCurve(simulationDailyYields);
        PortfolioMetrics metrics = PortfolioCalculationUtil
                .buildMetricsFromYields(simulationDailyYields, metric.totalValue());

        return new PortfolioCurve(curve, metrics);
    }

    private List<BigDecimal> fetchDailyYields(String fundCode, int analysisWindow, LocalDate dataDate) {
        LocalDate startDate = dataDate.minusDays(analysisWindow);
        String dates = startDate.format(DATE_FORMAT) + "," + dataDate.format(DATE_FORMAT);
        FundDailyReturnResponse response = infinaService.getFundDailyReturn(fundCode, dates);
        return response.dailyYields();
    }

    @Override
    public void attachSnapshot(MetricsHolder holder, String fundCode, int analysisWindow,
                               Map<AssetCategory, BigDecimal> weights) {
        try {
            PortfolioCurve simulationCurve = calculateSimulation(fundCode, analysisWindow, weights);
            if (simulationCurve != null) {
                if (holder.getMetrics() == null) {
                    holder.setMetrics(new PerformanceMetrics());
                }
                holder.getMetrics().setSimulatedPortfolioValue(simulationCurve.metrics().currentValue());
                holder.getMetrics().setTotalReturnPct(simulationCurve.metrics().totalReturnPct());
                holder.getMetrics().setMaxDrawdownPct(simulationCurve.metrics().maxDrawdownPct());
                holder.getMetrics().setDailyVolatilityPct(simulationCurve.metrics().dailyVolatilityPct());
                holder.getMetrics().setAnalysisWindowDays(analysisWindow);
                holder.getMetrics().setBenchmarkDiffPct(calculateBenchmarkDiffPct(
                        fundCode, analysisWindow, simulationCurve.metrics().totalReturnPct()));
            }
        } catch (Exception e) {
            log.warn("Simulation snapshot failed. Reason: {}", e.getMessage());
        }
    }

    /**
     * Karar Geçmişi'ndeki "Benchmark farkı" satırı bu alandan okunur. Hesap Fon Dashboard'daki
     * {@code benchmarkDiffBps} ile aynı (portföy getirisi - benchmark getirisi); tek fark, burada
     * mevcut portföy yerine kararın simülasyon portföyü kıyaslanıyor — metrik zaten o karara
     * iliştiriliyor. Puan (yüzde puanı) cinsinden, bps'e çevrilmeden saklanır.
     */
    private BigDecimal calculateBenchmarkDiffPct(String fundCode, int analysisWindow,
                                                 BigDecimal simulationReturnPct) {
        if (simulationReturnPct == null) {
            return null;
        }

        String period = PERIOD_PREFIX + analysisWindow + PERIOD_SUFFIX;
        BigDecimal benchmarkReturnPct = fundPeriodMetricService
                .getLatestByFundCodeAndPeriod(fundCode, period)
                .benchmarkReturn();

        if (benchmarkReturnPct == null) {
            log.debug("No benchmark return for fund {} period {}; benchmark diff left empty.", fundCode, period);
            return null;
        }

        return simulationReturnPct.subtract(benchmarkReturnPct).setScale(2, RoundingMode.HALF_UP);
    }

    private Optional<BigDecimal> resolveCurrentStockWeight(String fundCode) {
        return fundDistributionService.getLatestByFundCode(fundCode).stream()
                .filter(d -> AssetCategoryConverter.STOCK_DB.equals(d.category()))
                .map(FundDistributionResponse::weight)
                .findFirst()
                .filter(w -> w.signum() != 0);
    }
}
