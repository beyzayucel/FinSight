package com.akademi.finsight.fund.performancecomparison.util;

import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioMetrics;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PortfolioCalculationUtil {

    private static final int SCALE = 6;
    private static final int MIN_SAMPLE_SIZE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /**
     * Kümülatif getiri serisinden günlük getirileri türetir.
     *
     * <pre>
     *   dailyReturn = (100 + cum(t)) / (100 + cum(t-1)) - 1
     *
     *   100 eklenir çünkü kümülatif getiri yüzde cinsinden gelir:
     *   cum=0 → hiç değişim yok → 100+0=100 (baz)
     *   cum=3 → %3 artmış    → 100+3=103
     *
     *   Örnek: kümülatifler = [0, 2, 3.5]
     *          gün1 = (100+2)/(100+0) - 1 = 102/100 - 1 = 0.02
     *          gün2 = (100+3.5)/(100+2) - 1 = 103.5/102 - 1 ≈ 0.0147
     * </pre>
     */
    public static List<BigDecimal> deriveDailyReturnsFromCumulative(List<BigDecimal> cumulativeReturns) {
        if (cumulativeReturns.size() < MIN_SAMPLE_SIZE) {
            return List.of();
        }

        List<BigDecimal> dailyReturns = new ArrayList<>(cumulativeReturns.size() - 1);

        for (int i = 1; i < cumulativeReturns.size(); i++) {
            BigDecimal previous = HUNDRED.add(cumulativeReturns.get(i - 1));
            BigDecimal current = HUNDRED.add(cumulativeReturns.get(i));

            if (previous.signum() == 0) {
                dailyReturns.add(BigDecimal.ZERO);
                continue;
            }

            BigDecimal dailyReturn = current.divide(previous, SCALE, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE);
            dailyReturns.add(dailyReturn);
        }
        return dailyReturns;
    }

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
     * Fonun günlük getirilerinden simülasyon portföyünün günlük getirilerini türetir.
     * Kategori bazlı getiri API'si olmadığı için yaklaşık hesap kullanılır.
     *
     * <pre>
     *   Varsayım: repo/future getirisi ≈ 0, fon getirisi ≈ hisse ağırlığı × hisse getirisi
     *
     *   1. Hisse getirisini türet:
     *      hisseGetirisi = fonGetirisi / mevcutHisseAğırlığı
     *
     *   2. Simülasyon ağırlıklarıyla yeni getiriyi hesapla:
     *      simGetiri = simHisseAğırlığı × hisseGetirisi + simFonAğırlığı × fonGetirisi
     *
     *   Örnek: fonGetirisi=%1.2, mevcutHisseAğırlığı=%60
     *          simülasyon: hisse=%40, fon=%30
     *
     *          hisseGetirisi = 1.2 / 0.60 = %2.0
     *          simGetiri = 0.40 × 2.0 + 0.30 × 1.2 = 0.80 + 0.36 = %1.16
     * </pre>
     *
     * TODO: Infina kategori bazlı günlük getiri API'si gelince bu metot kalkacak, doğrudan gerçek getiriler kullanılacak.
     */
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

    /**
     * Günlük getirilerden maksimum düşüşü (peak-to-trough) hesaplar.
     *
     * <pre>
     *   Her adımda kümülatif değeri güncelle, zirveyi takip et.
     *   drawdown = (peak - cumulative) / peak
     *   En büyük drawdown saklanır, sonuç yüzde olarak döner.
     *
     *   Örnek: günlük getiriler = [+0.02, +0.03, -0.04, +0.01]
     *          kümülatif:  1.0 → 1.02 → 1.0506 → 1.0086 → 1.0187
     *          zirve:      1.0 → 1.02 → 1.0506 → 1.0506 → 1.0506
     *          drawdown:    0     0      0        (1.0506-1.0086)/1.0506 ≈ %4.0
     *          maxDrawdown = %4.0
     * </pre>
     */
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
        return maxDrawdown.multiply(HUNDRED).negate().setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Günlük getirilerin standart sapmasını (volatilite) hesaplar.
     * Bessel düzeltmesi ile örneklem standart sapması kullanılır (n-1).
     *
     * <pre>
     *   1. Ortalama:  mean = (r1 + r2 + ... + rn) / n
     *   2. Farkların karesi: (r_i - mean)²
     *   3. Varyans:   var = Σ(r_i - mean)² / (n - 1)
     *   4. Std sapma: σ = √var × 100  (yüzdeye çevir)
     *
     *   Örnek: günlük getiriler = [0.02, -0.01, 0.03]
     *          mean = (0.02 + (-0.01) + 0.03) / 3 = 0.01333
     *          farklar² = (0.00667)² + (-0.02333)² + (0.01667)² = 0.000903
     *          var = 0.000903 / 2 = 0.000452
     *          σ = √0.000452 × 100 ≈ %2.12
     * </pre>
     */
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
