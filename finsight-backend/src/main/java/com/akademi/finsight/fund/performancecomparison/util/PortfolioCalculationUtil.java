package com.akademi.finsight.fund.performancecomparison.util;

import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioMetrics;
import com.akademi.finsight.fund.stockprice.entity.StockPriceHistory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PortfolioCalculationUtil {

    private static final int SCALE = 6;
    private static final int MIN_SAMPLE_SIZE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** Kümülatif getiri serisinden günlük getirileri türetir: dailyReturn(t) = (100+cum(t))/(100+cum(t-1)) - 1. */
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
     * Fonun günlük getirilerinden simülasyon portföyünün günlük getirilerini türetir (kategori bazlı, yaklaşık):
     * hisseGetirisi = fonGetirisi / mevcutHisseAğırlığı; simGetiri = simHisseAğırlığı×hisseGetirisi + simFonAğırlığı×fonGetirisi.
     * Hisse kırılımı verilmediğinde (deriveSimulationDailyReturnsFromStockPrices yerine) kullanılır.
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
     * Hisse kapanis fiyat serisinden gunluk getiri sozlugu (tarih -> getiri) turetir.
     * <p>
     * priceHistory zaten sadece islem gunlerini icerir (hafta sonu/tatil hic kayda girmez), bu yuzden
     * ardisik iki eleman arasindaki fark direkt "bir sonraki islem gunu getirisi" anlamina gelir -
     * takvim gunu farkiyla ugrasmaya gerek yok.
     *
     * <pre>
     *   getiri(t) = (price(t) - price(t-1)) / price(t-1)
     * </pre>
     */
    public static Map<LocalDate, BigDecimal> deriveStockDailyReturns(List<StockPriceHistory> priceHistory) {
        Map<LocalDate, BigDecimal> returns = new LinkedHashMap<>();
        if (priceHistory.size() < MIN_SAMPLE_SIZE) {
            return returns;
        }

        for (int i = 1; i < priceHistory.size(); i++) {
            BigDecimal previous = priceHistory.get(i - 1).getClosePrice();
            BigDecimal current = priceHistory.get(i).getClosePrice();

            if (previous.signum() == 0) {
                returns.put(priceHistory.get(i).getDataDate(), BigDecimal.ZERO);
                continue;
            }

            BigDecimal dailyReturn = current.divide(previous, SCALE, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE);
            returns.put(priceHistory.get(i).getDataDate(), dailyReturn);
        }
        return returns;
    }

    /**
     * Fonun gunluk getirilerinden simulasyon portfoyunun gunluk getirilerini turetir - top-N hisse icin
     * GERCEK kapanis fiyati getirisi, geri kalan STOCK sepeti ("Others") icin eski yaklasik yontem
     * (fonGetirisi/mevcutHisseKategoriAgirligi) kullanilir. STOCK-disi kategoriler (FUND/REPO/FUTURE)
     * kendi aralarinda ayristirilamaz (kategori bazli getiri API'si yok), bu yuzden getirileri 0 kabul
     * edilir - ama AGIRLIKLARI (1 - simStockCategoryFraction) tam olarak hesaba katilir.
     * <p>
     * Invariant: agirliklar (hem kategori hem hisse) mevcut degerlerden hic degismezse, bu metot fonun
     * gercek toplam getirisini birebir geri uretir:
     * simReturn = simStockFraction × (fundReturn/currentStockFraction) + (1-simStockFraction) × 0
     *           = currentStockFraction × (fundReturn/currentStockFraction)   [simStockFraction=currentStockFraction oldugunda]
     *           = fundReturn
     * Onceki surumde STOCK-disi agirlik icin sadece FUND kategorisi ozel ele aliniyordu (fundReturn ile
     * carpiliyordu), REPO/FUTURE agirligi toplama hic girmiyordu - bu invariant'i bozuyordu (agirlik
     * degismese bile sonuc fondan kucuk bir miktar sapiyordu). Simdi STOCK-disi TUM agirlik (FUND+REPO+FUTURE)
     * tek fraksiyonda toplanip getirisi 0 sayiliyor - boylece invariant tam saglanir.
     * Detaylar icin bkz. calculateSleeveReturnForDay ve calculateOthersReturn.
     */
    public static List<BigDecimal> deriveSimulationDailyReturnsFromStockPrices(
            List<BigDecimal> fundDailyYields,
            List<LocalDate> dates,
            Map<String, Map<LocalDate, BigDecimal>> stockReturnsByAssetCode,
            Map<String, BigDecimal> simStockWeights,
            BigDecimal currentStockCategoryWeight,
            Map<AssetCategory, BigDecimal> simulationWeights) {

        BigDecimal currentStockCategoryFraction = toFraction(currentStockCategoryWeight);
        BigDecimal simStockCategoryFraction = toFraction(simulationWeights.getOrDefault(AssetCategory.STOCK, BigDecimal.ZERO));

        Map<String, BigDecimal> simStockFractions = toFractionMap(simStockWeights);
        BigDecimal simOthersFraction = calculateOthersFraction(simStockFractions);

        // Her hisse icin son bilinen getiriyi tutar - o gun veri yoksa forward-fill (0 getiri) uygulanir.
        Map<String, BigDecimal> lastKnownReturn = new LinkedHashMap<>();
        for (String assetCode : stockReturnsByAssetCode.keySet()) {
            lastKnownReturn.put(assetCode, BigDecimal.ZERO);
        }

        List<BigDecimal> simulationYields = new ArrayList<>(fundDailyYields.size());

        for (int i = 0; i < fundDailyYields.size(); i++) {
            BigDecimal fundReturn = fundDailyYields.get(i);
            LocalDate date = dates.get(i);

            BigDecimal sleeveReturn = calculateSleeveReturnForDay(
                    date, simStockFractions, stockReturnsByAssetCode, lastKnownReturn);
            sleeveReturn = sleeveReturn.add(calculateOthersReturn(
                    fundReturn, currentStockCategoryFraction, simOthersFraction));

            // STOCK-disi kategorilerin (FUND+REPO+FUTURE) agirligi tam sayilir, getirisi 0 varsayilir -
            // bu carpani formulden dusuyor, ama agirlik payinin hic kaybolmamasini (invariant) garanti eder.
            BigDecimal simReturn = simStockCategoryFraction.multiply(sleeveReturn, MathContext.DECIMAL64);
            simulationYields.add(simReturn);
        }

        return simulationYields;
    }

    /** Yuzdeyi (0-100) kesire (0-1) cevirir. */
    private static BigDecimal toFraction(BigDecimal percentage) {
        return percentage.divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }

    /** Bir agirlik haritasindaki tum yuzdeleri kesire cevirir. */
    private static Map<String, BigDecimal> toFractionMap(Map<String, BigDecimal> weights) {
        Map<String, BigDecimal> fractions = new LinkedHashMap<>();
        weights.forEach((assetCode, weight) -> fractions.put(assetCode, toFraction(weight)));
        return fractions;
    }

    /** Top-N disinda kalan pay: 1 - Σ(top-N kesirleri). Sleeve-relative olctugu icin (SUM=1) gecerli. */
    private static BigDecimal calculateOthersFraction(Map<String, BigDecimal> topNFractions) {
        BigDecimal sum = topNFractions.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return BigDecimal.ONE.subtract(sum);
    }

    /**
     * Belirli bir gun icin top-N hisselerin agirlikli getiri toplamini hesaplar (Others haric).
     * <pre>Σ[simAgirlik_i × gercekGetiri_i(t)]</pre>
     * Hissenin o gun fiyati yoksa (islem durmasi vb.) son bilinen getiri kullanilir (forward-fill).
     */
    private static BigDecimal calculateSleeveReturnForDay(LocalDate date,
                                                           Map<String, BigDecimal> simStockFractions,
                                                           Map<String, Map<LocalDate, BigDecimal>> stockReturnsByAssetCode,
                                                           Map<String, BigDecimal> lastKnownReturn) {
        BigDecimal sleeveReturn = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : simStockFractions.entrySet()) {
            String assetCode = entry.getKey();
            BigDecimal weightFraction = entry.getValue();

            Map<LocalDate, BigDecimal> assetReturns = stockReturnsByAssetCode.get(assetCode);
            BigDecimal stockReturn = assetReturns != null && assetReturns.containsKey(date)
                    ? assetReturns.get(date)
                    : lastKnownReturn.getOrDefault(assetCode, BigDecimal.ZERO);
            lastKnownReturn.put(assetCode, stockReturn);

            sleeveReturn = sleeveReturn.add(weightFraction.multiply(stockReturn, MathContext.DECIMAL64));
        }
        return sleeveReturn;
    }

    /**
     * Top-N disi ("Others") hisselerin getiri katkisini eski yaklasik yontemle hesaplar - fiyat verisi
     * olmadigi icin fon getirisinden geriye turetilir: othersGetiri(t) = fonGetirisi(t) / mevcutHisseKategoriAgirligi.
     */
    private static BigDecimal calculateOthersReturn(BigDecimal fundReturn,
                                                     BigDecimal currentStockCategoryFraction,
                                                     BigDecimal simOthersFraction) {
        if (currentStockCategoryFraction.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal othersReturn = fundReturn.divide(currentStockCategoryFraction, SCALE, RoundingMode.HALF_UP);
        return simOthersFraction.multiply(othersReturn, MathContext.DECIMAL64);
    }

    /** Günlük getirilerden maksimum düşüşü (peak-to-trough) hesaplar: drawdown = (peak - cumulative) / peak. */
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

    /** Günlük getirilerin standart sapmasını (volatilite) hesaplar. Bessel düzeltmeli örneklem std sapması (n-1). */
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
