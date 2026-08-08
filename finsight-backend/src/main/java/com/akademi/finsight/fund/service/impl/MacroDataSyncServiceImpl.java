package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.entity.MacroData;
import com.akademi.finsight.fund.repository.MacroDataRepository;
import com.akademi.finsight.fund.service.MacroDataSyncService;
import com.akademi.finsight.integration.infina.dto.response.fx.FxPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.economic.EconomicPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.index.IndexPriceResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.akademi.finsight.fund.constant.MacroConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MacroDataSyncServiceImpl implements MacroDataSyncService {

    private final InfinaService infinaService;
    private final MacroDataRepository macroDataRepository;

    private static final int RETURN_SCALE = 12;

    @Override
    @Transactional
    public MacroData sync(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            date = date.minusDays(1);
        } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(2);
        }

        log.info("Starting macro data sync for date: {}", date);

        String dateRange = "[" + date.minusDays(5) + "," + date + "]";

        BigDecimal usdReturn = calculateFxReturn(USD_TRY_CODE, dateRange, date);
        BigDecimal goldReturn = calculateIndexReturn(GOLD_CODE, dateRange, date);
        BigDecimal brentReturn = calculateIndexReturn(BRENT_CODE, dateRange, date);
        BigDecimal us10yReturn = calculateIndexReturn(BOND_10Y_CODE, dateRange, date);
        BigDecimal annualInflation = fetchEconomicPrice(INFLATION_CODE, date);
        BigDecimal policyRate = fetchEconomicPrice(POLICY_RATE_CODE, date);

        MacroData macroData = MacroData.builder()
                .date(date)
                .usdReturn(usdReturn)
                .goldReturn(goldReturn)
                .brentReturn(brentReturn)
                .us10yReturn(us10yReturn)
                .cdsSpreadBps(DEFAULT_CDS)
                .annualInflation(annualInflation)
                .policyRate(policyRate)
                .build();

        MacroData saved = macroDataRepository.save(macroData);
        log.info("Macro data synced successfully for date: {}", date);
        return saved;
    }

    private BigDecimal calculateFxReturn(String assetCode, String dateRange, LocalDate targetDate) {
        try {
            List<FxPriceResponse> rates = infinaService.getFxPrices(assetCode, dateRange);
            return calculateDailyReturn(rates, assetCode, targetDate, FxPriceResponse::dataDate, FxPriceResponse::ask);
        } catch (Exception e) {
            log.error("Error calculating exchange rate return for {}", assetCode, e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateIndexReturn(String assetCode, String dateRange, LocalDate targetDate) {
        try {
            List<IndexPriceResponse> rates = infinaService.getIndexPrices(assetCode, dateRange);
            return calculateDailyReturn(rates, assetCode, targetDate, IndexPriceResponse::dataDate, IndexPriceResponse::closePrice);
        } catch (Exception e) {
            log.error("Error calculating index price return for {}", assetCode, e);
            return BigDecimal.ZERO;
        }
    }

    private <T> BigDecimal calculateDailyReturn(
            List<T> items,
            String assetCode,
            LocalDate targetDate,
            Function<T, LocalDate> dateExtractor,
            Function<T, BigDecimal> priceExtractor) {

        if (Objects.isNull(items) || items.isEmpty()) {
            log.warn("No price data returned for {}", assetCode);
            return BigDecimal.ZERO;
        }

        List<T> sorted = items.stream()
                .filter(item -> item != null && dateExtractor.apply(item) != null && priceExtractor.apply(item) != null)
                .sorted(Comparator.comparing(dateExtractor))
                .toList();

        int targetIndex = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (Objects.equals(dateExtractor.apply(sorted.get(i)), targetDate)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex <= 0) {
            log.warn("Insufficient price history for {} on date {}", assetCode, targetDate);
            return BigDecimal.ZERO;
        }

        BigDecimal priceToday = priceExtractor.apply(sorted.get(targetIndex));
        BigDecimal pricePrev = priceExtractor.apply(sorted.get(targetIndex - 1));

        if (pricePrev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return priceToday.subtract(pricePrev).divide(pricePrev, RETURN_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal fetchEconomicPrice(String assetCode, LocalDate targetDate) {
        BigDecimal fallback = getDefaultEconomicPrice(assetCode);
        try {
            String dateRange = "[" + targetDate.minusMonths(2) + "," + targetDate + "]";
            List<EconomicPriceResponse> rates = infinaService.getEconomicPrices(assetCode, dateRange);
            if (rates == null || rates.isEmpty()) {
                log.warn("No economic data returned for {} in range {}, using fallback: {}", assetCode, dateRange, fallback);
                return fallback;
            }

            return rates.stream()
                    .filter(rate -> rate != null && rate.dataDate() != null && !rate.dataDate().isAfter(targetDate))
                    .max(Comparator.comparing(EconomicPriceResponse::dataDate))
                    .map(EconomicPriceResponse::price)
                    .filter(Objects::nonNull)
                    .orElse(fallback);
        } catch (Exception e) {
            log.error("Error fetching economic price for {}", assetCode, e);
            return fallback;
        }
    }

    private BigDecimal getDefaultEconomicPrice(String assetCode) {
        return switch (assetCode) {
            case INFLATION_CODE -> DEFAULT_INFLATION;
            case POLICY_RATE_CODE -> DEFAULT_POLICY_RATE;
            default -> BigDecimal.ZERO;
        };
    }
}
