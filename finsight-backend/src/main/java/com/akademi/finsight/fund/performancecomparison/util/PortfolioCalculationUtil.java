package com.akademi.finsight.fund.performancecomparison.util;

import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.CurvePoint;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioMetrics;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Portföy performans karşılaştırması için stateless hesaplama methodları.
İhtiyaç duyulan infina API'den alınacak servicelere erişebilirsek buradaki code azalacak!!!!!!!. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PortfolioCalculationUtil {

    private static final int SCALE = 6;
    private static final int MIN_SAMPLE_SIZE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** Compound kümülatif getiri eğrisi: {@code day(i) = (∏(1+r_j) - 1) × 100} */
    public static List<CurvePoint> buildCumulativeCurve(List<BigDecimal> dailyYields) {
        List<CurvePoint> points = new ArrayList<>(dailyYields.size() + 1);
        points.add(new CurvePoint(0, BigDecimal.ZERO));

        BigDecimal cumulative = BigDecimal.ONE;
        for (int i = 0; i < dailyYields.size(); i++) {
            cumulative = cumulative.multiply(
                    BigDecimal.ONE.add(dailyYields.get(i)),
                    MathContext.DECIMAL64
            );
            BigDecimal cumulativeReturnPct = cumulative.subtract(BigDecimal.ONE)
                    .multiply(HUNDRED)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            points.add(new CurvePoint(i + 1, cumulativeReturnPct));
        }
        return points;
    }

    /** Compound return + drawdown + volatilite metrikleri. */
    public static PortfolioMetrics buildMetricsFromYields(List<BigDecimal> dailyYields,
                                                          BigDecimal portfolioValue) {
        BigDecimal cumulative = BigDecimal.ONE;
        for (BigDecimal dailyYield : dailyYields) {
            cumulative = cumulative.multiply(BigDecimal.ONE.add(dailyYield), MathContext.DECIMAL64);
        }

        BigDecimal totalReturnPct = cumulative.subtract(BigDecimal.ONE)
                .multiply(HUNDRED)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal currentValue = portfolioValue.multiply(cumulative, MathContext.DECIMAL64)
                .setScale(2, RoundingMode.HALF_UP);

        return new PortfolioMetrics(currentValue, totalReturnPct,
                calculateMaxDrawdown(dailyYields), calculateDailyVolatility(dailyYields));
    }

    /**
     * Residual decomposition — Infina kategori bazlı getiri vermediği için
     * fon getirisinden hisse getirisini türetip simülasyon getirisi hesaplar.
     *
     * <p>Varsayım: REPO ve FUTURE günlük getirisi ≈ 0</p>
     * <pre>
     *   r_stock ≈ R_fund / w_stock
     *   R_sim   = sim_w_stock × r_stock + sim_w_fund × R_fund
     * </pre>
     */
    // TODO: Infina kategori bazlı günlük getiri API'si gelince bu metot kalkacak, doğrudan gerçek getiriler kullanılacak.
    public static List<BigDecimal> deriveSimulationDailyReturns(
            List<BigDecimal> fundDailyYields,
            BigDecimal currentStockWeight,
            Map<AssetCategory, BigDecimal> simulationWeights) {

        BigDecimal stockFraction = currentStockWeight.divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
        BigDecimal simStockFraction = simulationWeights
                .getOrDefault(AssetCategory.STOCK, BigDecimal.ZERO)
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
        BigDecimal simFundFraction = simulationWeights
                .getOrDefault(AssetCategory.FUND, BigDecimal.ZERO)
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

        List<BigDecimal> simulationYields = new ArrayList<>(fundDailyYields.size());

        for (BigDecimal fundReturn : fundDailyYields) {
            BigDecimal stockReturn = fundReturn.divide(stockFraction, SCALE, RoundingMode.HALF_UP);
            BigDecimal simReturn = simStockFraction.multiply(stockReturn, MathContext.DECIMAL64)
                    .add(simFundFraction.multiply(fundReturn, MathContext.DECIMAL64));
            simulationYields.add(simReturn);
        }

        return simulationYields;
    }

    /** Toplam getiriyi günlere eşit dağıtır: {@code r_daily = (1+R/100)^(1/n) - 1} */
    // TODO: Infina benchmark günlük getiri API'si gelince bu interpolasyon kalkacak, gerçek günlük benchmark getirileri kullanılacak.
    public static List<BigDecimal> interpolateBenchmarkDailyYields(BigDecimal benchmarkReturnPct,
                                                                    int dayCount) {
        double totalReturn = benchmarkReturnPct.doubleValue() / 100.0;
        double dailyReturn = Math.pow(1.0 + totalReturn, 1.0 / dayCount) - 1.0;

        BigDecimal dailyYield = BigDecimal.valueOf(dailyReturn);
        List<BigDecimal> yields = new ArrayList<>(dayCount);
        for (int i = 0; i < dayCount; i++) {
            yields.add(dailyYield);
        }
        return yields;
    }

    /** Peak-to-trough max drawdown (%): {@code (peak - cumulative) / peak} */
    public static BigDecimal calculateMaxDrawdown(List<BigDecimal> dailyYields) {
        if (dailyYields.size() < MIN_SAMPLE_SIZE) {
            return BigDecimal.ZERO;
        }

        BigDecimal cumulative = BigDecimal.ONE;
        BigDecimal peak = BigDecimal.ONE;
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        for (BigDecimal dailyYield : dailyYields) {
            cumulative = cumulative.multiply(BigDecimal.ONE.add(dailyYield), MathContext.DECIMAL64);
            if (cumulative.compareTo(peak) > 0) {
                peak = cumulative;
            }
            BigDecimal drawdown = peak.subtract(cumulative)
                    .divide(peak, SCALE, RoundingMode.HALF_UP);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }
        return maxDrawdown.multiply(HUNDRED).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Sample standard deviation (%): {@code σ = √(Σ(r_i - mean)² / (n-1)) × 100} */
    public static BigDecimal calculateDailyVolatility(List<BigDecimal> dailyYields) {
        if (dailyYields.size() < MIN_SAMPLE_SIZE) {
            return BigDecimal.ZERO;
        }

        double[] values = dailyYields.stream().mapToDouble(BigDecimal::doubleValue).toArray();
        double mean = 0;
        for (double v : values) {
            mean += v;
        }
        mean /= values.length;

        double sumSquaredDiff = 0;
        for (double v : values) {
            sumSquaredDiff += (v - mean) * (v - mean);
        }
        double stdDev = Math.sqrt(sumSquaredDiff / (values.length - 1));

        return BigDecimal.valueOf(stdDev)
                .multiply(HUNDRED)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
