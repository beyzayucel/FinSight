package com.akademi.finsight.ai.model.service.impl;

import com.akademi.finsight.ai.model.service.ModelDataSyncService;
import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.ai.model.dto.response.FundPriceDataResponse;
import com.akademi.finsight.ai.model.dto.response.MarketDataResponse;
import com.akademi.finsight.ai.model.dto.response.ModelDataSyncResponse;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.ai.model.entity.FundPriceData;
import com.akademi.finsight.ai.model.entity.MarketData;
import com.akademi.finsight.ai.model.repository.FundPriceDataRepository;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.ai.model.repository.MarketDataRepository;
import com.akademi.finsight.ai.model.service.CdsDataService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static com.akademi.finsight.ai.model.constant.MarketConstants.*;

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
    private final CdsDataService cdsDataService;

    private static final int MAX_BACKWARD_SEARCH_DAYS = 10;

    @Override
    @Transactional
    public ModelDataSyncResponse sync() {
        log.info("Starting unified model data sync (Market Data + Fund Price Data)");
        FundPriceData fundPriceData = syncFundPriceData();
        MarketData marketData = syncMarketData(fundPriceData.getDataDate());
        return new ModelDataSyncResponse(
                MarketDataResponse.from(marketData),
                FundPriceDataResponse.from(fundPriceData)
        );
    }

    private MarketData syncMarketData(LocalDate effectiveDate) {
        log.info("Starting market data sync for date={}", effectiveDate);

        MarketData marketData = marketDataRepository.findByDataDate(effectiveDate)
                .orElseGet(() -> MarketData.builder().dataDate(effectiveDate).build());

        marketData.setDataDate(effectiveDate);
        marketData.setUsdReturn(calculateFxReturn(USD_TRY_CODE, effectiveDate));
        marketData.setGoldReturn(calculateIndexReturn(GOLD_CODE, effectiveDate));
        marketData.setBrentReturn(calculateIndexReturn(BRENT_CODE, effectiveDate));
        marketData.setUs10yReturn(calculateIndexReturn(BOND_10Y_CODE, effectiveDate));
        marketData.setCdsSpreadBps(cdsDataService.getCdsSpreadForDate(effectiveDate));
        marketData.setAnnualInflation(fetchEconomicPrice(INFLATION_CODE, effectiveDate));
        marketData.setPolicyRate(fetchEconomicPrice(POLICY_RATE_CODE, effectiveDate));

        MarketData saved = marketDataRepository.save(marketData);
        log.info("Market data synced successfully for date: {}", effectiveDate);
        return saved;
    }

    private FundPriceData syncFundPriceData() {
        String fundCode = fundProperties.getCode();
        LocalDate targetDate = resolveTargetDate();
        log.info("Starting fund price sync with backward search for fundCode={}, targetDate={}", fundCode, targetDate);

        List<FundPriceResponse> prices = fetchFundPricesWithBackwardSearch(fundCode, targetDate);

        if (prices.isEmpty()) {
            throw new IllegalStateException("Could not fetch fund price data for fundCode: " + fundCode);
        }

        FundPriceResponse targetPrice = resolveTargetPrice(prices);
        Fund fund = getOrCreateFund(fundCode);
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
            List<FxPriceResponse> rates = fetchTwoConsecutivePrices(targetDate, dateStr -> infinaService.getFxPrices(assetCode, dateStr));
            return calculateDailyReturn(rates, targetDate, assetCode, FxPriceResponse::dataDate, FxPriceResponse::ask);
        } catch (Exception e) {
            log.error("Error calculating exchange rate return for asset: {}", assetCode, e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateIndexReturn(String assetCode, LocalDate targetDate) {
        try {
            List<IndexPriceResponse> rates = fetchTwoConsecutivePrices(targetDate, dateStr -> infinaService.getIndexPrices(assetCode, dateStr));
            return calculateDailyReturn(rates, targetDate, assetCode, IndexPriceResponse::dataDate, IndexPriceResponse::closePrice);
        } catch (Exception e) {
            log.error("Error calculating index price return for asset: {}", assetCode, e);
            return BigDecimal.ZERO;
        }
    }

    private <T> List<T> fetchTwoConsecutivePrices(LocalDate targetDate, Function<String, List<T>> fetcher) {
        List<T> found = new ArrayList<>();
        LocalDate current = targetDate;
        for (int i = 0; i < MAX_BACKWARD_SEARCH_DAYS && found.size() < 2; i++) {
            try {
                List<T> rates = fetcher.apply(current.toString());
                if (rates != null && !rates.isEmpty()) {
                    found.addAll(rates);
                }
            } catch (Exception e) {
                log.debug("No price data for date: {}", current);
            }
            current = current.minusDays(1);
        }
        return found;
    }

    private <T> BigDecimal calculateDailyReturn(
            List<T> items,
            LocalDate targetDate,
            String assetCode,
            Function<T, LocalDate> dateExtractor,
            Function<T, BigDecimal> priceExtractor) {

        if (Objects.isNull(items) || items.isEmpty()) {
            log.warn("No price data returned for {}", assetCode);
            return BigDecimal.ZERO;
        }

        List<T> sorted = items.stream()
                .filter(item -> item != null && dateExtractor.apply(item) != null && priceExtractor.apply(item) != null)
                .filter(item -> !dateExtractor.apply(item).isAfter(targetDate))
                .sorted(Comparator.comparing(dateExtractor))
                .toList();

        if (sorted.size() < 2) {
            log.warn("Insufficient data points (< 2) for {} on or before {}, cannot calculate daily return, defaulting to 0", assetCode, targetDate);
            return BigDecimal.ZERO;
        }

        T todayItem = sorted.getLast();
        T prevItem = sorted.get(sorted.size() - 2);

        BigDecimal priceToday = priceExtractor.apply(todayItem);
        BigDecimal pricePrev = priceExtractor.apply(prevItem);

        if (pricePrev == null || pricePrev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyReturn = priceToday.subtract(pricePrev).divide(pricePrev, RETURN_SCALE, RoundingMode.HALF_UP);
        log.info("Calculated daily return for {}: todayDate={}, todayPrice={}, prevDate={}, prevPrice={}, return={}",
                assetCode, dateExtractor.apply(todayItem), priceToday, dateExtractor.apply(prevItem), pricePrev, dailyReturn);

        return dailyReturn;
    }

    private BigDecimal fetchEconomicPrice(String assetCode, LocalDate targetDate) {
        BigDecimal fallback = getDefaultEconomicPrice(assetCode);
        try {
            List<EconomicPriceResponse> rates = infinaService.getEconomicPrices(assetCode, null);
            if (rates == null || rates.isEmpty()) {
                log.warn("No economic data returned for {}, using fallback: {}", assetCode, fallback);
                return fallback;
            }

            return rates.stream()
                    .filter(rate -> rate != null && rate.price() != null && rate.dataDate() != null)
                    .filter(rate -> !rate.dataDate().isAfter(targetDate))
                    .max(Comparator.comparing(EconomicPriceResponse::dataDate))
                    .map(EconomicPriceResponse::price)
                    .orElse(fallback);
        } catch (Exception e) {
            log.error("Error fetching economic price for asset: {}", assetCode, e);
            return fallback;
        }
    }

    private BigDecimal getDefaultEconomicPrice(String assetCode) {
        return switch (assetCode) {
            case INFLATION_CODE -> null;
            case POLICY_RATE_CODE -> null;
            default -> BigDecimal.ZERO;
        };
    }
}
