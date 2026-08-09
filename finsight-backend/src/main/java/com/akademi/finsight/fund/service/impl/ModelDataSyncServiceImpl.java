package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.dto.response.ModelDataSyncResponse;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.FundPriceData;
import com.akademi.finsight.fund.entity.MarketData;
import com.akademi.finsight.fund.repository.FundPriceDataRepository;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.repository.MarketDataRepository;
import com.akademi.finsight.fund.service.ModelDataSyncService;
import com.akademi.finsight.integration.infina.dto.response.economic.EconomicPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.fx.FxPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.index.IndexPriceResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.akademi.finsight.fund.constant.MarketConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelDataSyncServiceImpl implements ModelDataSyncService {

    private static final int RETURN_SCALE = 12;

    private final InfinaService infinaService;
    private final MarketDataRepository marketDataRepository;
    private final FundRepository fundRepository;
    private final FundPriceDataRepository fundPriceDataRepository;
    private final FundProperties fundProperties;

    private static final int MAX_BACKWARD_SEARCH_DAYS = 10;

    @Override
    @Transactional
    public ModelDataSyncResponse sync() {
        log.info("Starting unified model data sync (Market Data + Fund Price Data)");
        MarketData marketData = syncMarketData();
        FundPriceData fundPriceData = syncFundPriceData();
        return new ModelDataSyncResponse(marketData, fundPriceData);
    }

    @Override
    @Transactional
    public MarketData syncMarketData() {
        LocalDate targetDate = resolveTargetDate();
        log.info("Starting market data sync with backward search from targetDate={}", targetDate);

        List<IndexPriceResponse> goldRates = fetchIndexRatesWithBackwardSearch(GOLD_CODE, targetDate);
        LocalDate actualDate = resolveMarketDate(goldRates).orElse(targetDate);

        MarketData marketData = MarketData.builder()
                .date(actualDate)
                .usdReturn(calculateFxReturn(USD_TRY_CODE, targetDate))
                .goldReturn(calculateDailyReturn(goldRates, GOLD_CODE, IndexPriceResponse::dataDate, IndexPriceResponse::closePrice))
                .brentReturn(calculateIndexReturn(BRENT_CODE, targetDate))
                .us10yReturn(calculateIndexReturn(BOND_10Y_CODE, targetDate))
                .cdsSpreadBps(DEFAULT_CDS)
                .annualInflation(fetchEconomicPrice(INFLATION_CODE, targetDate))
                .policyRate(fetchEconomicPrice(POLICY_RATE_CODE, targetDate))
                .build();

        MarketData saved = marketDataRepository.save(marketData);
        log.info("Market data synced successfully for resolved date: {}", actualDate);
        return saved;
    }

    private List<IndexPriceResponse> fetchIndexRatesWithBackwardSearch(String assetCode, LocalDate targetDate) {
        LocalDate current = targetDate;
        for (int i = 0; i < MAX_BACKWARD_SEARCH_DAYS; i++) {
            try {
                List<IndexPriceResponse> rates = infinaService.getIndexPrices(assetCode, current.toString());
                if (rates != null && !rates.isEmpty()) {
                    log.info("Index rates found for asset: {} on date: {}", assetCode, current);
                    return rates;
                }
            } catch (Exception e) {
                log.debug("No index data for date: {}", current);
            }
            current = current.minusDays(1);
        }
        log.warn("No index data found in backward search for {}, using latest available", assetCode);
        return infinaService.getIndexPrices(assetCode, null);
    }

    private Optional<LocalDate> resolveMarketDate(List<IndexPriceResponse> rates) {
        if (rates == null) return Optional.empty();
        return rates.stream()
                .filter(rate -> rate != null && rate.dataDate() != null)
                .map(IndexPriceResponse::dataDate)
                .max(Comparator.naturalOrder());
    }

    @Override
    @Transactional
    public FundPriceData syncFundPriceData() {
        String fundCode = fundProperties.getCode();
        LocalDate targetDate = resolveTargetDate();
        log.info("Starting fund price sync with backward search for fundCode={}, targetDate={}", fundCode, targetDate);

        Fund fund = getOrCreateFund(fundCode);
        List<FundPriceResponse> prices = fetchFundPricesWithBackwardSearch(fundCode, targetDate);

        if (prices == null || prices.isEmpty()) {
            log.warn("No price data returned from FonFiyat for fund: {}", fundCode);
            return null;
        }

        FundPriceResponse targetPrice = resolveTargetPrice(prices);
        LocalDate dataDate = targetPrice.dataDate() != null ? targetPrice.dataDate() : targetDate;

        FundPriceData entity = findOrCreatePriceEntity(fund, dataDate);
        populatePriceData(entity, targetPrice, Instant.now());

        FundPriceData saved = fundPriceDataRepository.save(entity);
        log.info("Fund price data synced successfully: fundCode={}, dataDate={}, activeValue={}, portfolioValue={}, cashValue={}, investorCount={}",
                fundCode, dataDate, saved.getActiveValue(), saved.getPortfolioValue(), saved.getCashValue(), saved.getInvestorCount());

        return saved;
    }

    private List<FundPriceResponse> fetchFundPricesWithBackwardSearch(String fundCode, LocalDate targetDate) {
        LocalDate current = targetDate;
        for (int i = 0; i < MAX_BACKWARD_SEARCH_DAYS; i++) {
            try {
                List<FundPriceResponse> prices = infinaService.getFundPrices(fundCode, current.toString());
                if (prices != null && !prices.isEmpty()) {
                    log.info("Fund price data found for fund: {} on date: {}", fundCode, current);
                    return prices;
                }
            } catch (Exception e) {
                log.debug("No fund price for date: {}", current);
            }
            current = current.minusDays(1);
        }
        log.warn("No fund prices found in backward search for {}, using latest available", fundCode);
        return infinaService.getFundPrices(fundCode, null);
    }

    private Fund getOrCreateFund(String fundCode) {
        return fundRepository.findByCode(fundCode)
                .orElseGet(() -> fundRepository.save(Fund.builder().code(fundCode).build()));
    }

    private FundPriceResponse resolveTargetPrice(List<FundPriceResponse> prices) {
        return prices.stream()
                .filter(price -> price != null && price.dataDate() != null)
                .max(Comparator.comparing(FundPriceResponse::dataDate))
                .orElse(prices.getFirst());
    }

    private FundPriceData findOrCreatePriceEntity(Fund fund, LocalDate dataDate) {
        return fundPriceDataRepository
                .findByFundIdAndDataDate(fund.getId(), dataDate)
                .orElseGet(() -> FundPriceData.builder()
                        .fund(fund)
                        .dataDate(dataDate)
                        .build());
    }

    private void populatePriceData(FundPriceData entity, FundPriceResponse price, Instant fetchedAt) {
        entity.setPrice(Optional.ofNullable(price.price()).orElse(BigDecimal.ZERO));
        entity.setActiveValue(price.activeValue());
        entity.setPortfolioValue(price.portfolioValue());
        entity.setCashValue(price.cashValue());
        entity.setInvestorCount(price.investorCount());
        entity.setFetchedAt(fetchedAt);
    }

    private LocalDate resolveTargetDate() {
        int dataLagDays = fundProperties.getSync().getDataLagDays();
        return dataLagDays > 0 ? LocalDate.now().minusDays(dataLagDays) : LocalDate.now();
    }

    private BigDecimal calculateFxReturn(String assetCode, LocalDate targetDate) {
        try {
            List<FxPriceResponse> rates = fetchFxPricesWithBackwardSearch(assetCode, targetDate);
            return calculateDailyReturn(rates, assetCode, FxPriceResponse::dataDate, FxPriceResponse::ask);
        } catch (Exception e) {
            log.error("Error calculating exchange rate return for asset: {}", assetCode, e);
            return BigDecimal.ZERO;
        }
    }

    private List<FxPriceResponse> fetchFxPricesWithBackwardSearch(String assetCode, LocalDate targetDate) {
        LocalDate current = targetDate;
        for (int i = 0; i < MAX_BACKWARD_SEARCH_DAYS; i++) {
            try {
                List<FxPriceResponse> rates = infinaService.getFxPrices(assetCode, current.toString());
                if (rates != null && !rates.isEmpty()) {
                    return rates;
                }
            } catch (Exception e) {
                log.debug("No FX rates for date: {}", current);
            }
            current = current.minusDays(1);
        }
        return infinaService.getFxPrices(assetCode, null);
    }

    private BigDecimal calculateIndexReturn(String assetCode, LocalDate targetDate) {
        try {
            List<IndexPriceResponse> rates = fetchIndexRatesWithBackwardSearch(assetCode, targetDate);
            return calculateDailyReturn(rates, assetCode, IndexPriceResponse::dataDate, IndexPriceResponse::closePrice);
        } catch (Exception e) {
            log.error("Error calculating index price return for asset: {}", assetCode, e);
            return BigDecimal.ZERO;
        }
    }

    private <T> BigDecimal calculateDailyReturn(
            List<T> items,
            String assetCode,
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

        if (sorted.isEmpty()) {
            return BigDecimal.ZERO;
        }

        if (sorted.size() >= 2) {
            BigDecimal priceToday = priceExtractor.apply(sorted.getLast());
            BigDecimal pricePrev = priceExtractor.apply(sorted.get(sorted.size() - 2));

            if (pricePrev.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return priceToday.subtract(pricePrev).divide(pricePrev, RETURN_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal price = priceExtractor.apply(sorted.getFirst());
        return price != null ? price : BigDecimal.ZERO;
    }

    private BigDecimal fetchEconomicPrice(String assetCode, LocalDate targetDate) {
        BigDecimal fallback = getDefaultEconomicPrice(assetCode);
        try {
            List<EconomicPriceResponse> rates = fetchEconomicPricesWithBackwardSearch(assetCode, targetDate);
            if (rates == null || rates.isEmpty()) {
                log.warn("No economic data returned for {}, using fallback: {}", assetCode, fallback);
                return fallback;
            }

            return rates.stream()
                    .filter(rate -> rate != null && rate.price() != null)
                    .max(Comparator.comparing(EconomicPriceResponse::dataDate, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .map(EconomicPriceResponse::price)
                    .orElse(fallback);
        } catch (Exception e) {
            log.error("Error fetching economic price for asset: {}", assetCode, e);
            return fallback;
        }
    }

    private List<EconomicPriceResponse> fetchEconomicPricesWithBackwardSearch(String assetCode, LocalDate targetDate) {
        LocalDate current = targetDate;
        for (int i = 0; i < MAX_BACKWARD_SEARCH_DAYS; i++) {
            try {
                List<EconomicPriceResponse> rates = infinaService.getEconomicPrices(assetCode, current.toString());
                if (rates != null && !rates.isEmpty()) {
                    return rates;
                }
            } catch (Exception e) {
                log.debug("No economic rates for date: {}", current);
            }
            current = current.minusDays(1);
        }
        return infinaService.getEconomicPrices(assetCode, null);
    }

    private BigDecimal getDefaultEconomicPrice(String assetCode) {
        return switch (assetCode) {
            case INFLATION_CODE -> DEFAULT_INFLATION;
            case POLICY_RATE_CODE -> DEFAULT_POLICY_RATE;
            default -> BigDecimal.ZERO;
        };
    }
}
