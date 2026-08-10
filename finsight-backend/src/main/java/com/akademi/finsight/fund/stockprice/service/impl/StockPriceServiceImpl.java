package com.akademi.finsight.fund.stockprice.service.impl;

import com.akademi.finsight.fund.stockprice.entity.StockPriceHistory;
import com.akademi.finsight.fund.stockprice.repository.StockPriceHistoryRepository;
import com.akademi.finsight.fund.stockprice.service.StockPriceService;
import com.akademi.finsight.integration.infina.dto.response.stock.StockPriceResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockPriceServiceImpl implements StockPriceService {

    private static final String EQUITY_SUFFIX = ".E";

    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final InfinaService infinaService;

    @Override
    public void backfillIfMissing(String assetCode, LocalDate fromDate, LocalDate toDate) {
        if (stockPriceHistoryRepository.existsByAssetCode(assetCode)) {
            return;
        }

        List<StockPriceResponse> prices = infinaService.getStockPrices(toEquityCode(assetCode), null);
        if (prices.isEmpty()) {
            log.warn("Infina returned no price history for asset: assetCode={}", assetCode);
            return;
        }

        Instant fetchedAt = Instant.now();
        List<StockPriceHistory> rows = prices.stream()
                .filter(price -> !price.dataDate().isBefore(fromDate) && !price.dataDate().isAfter(toDate))
                .map(price -> toEntity(assetCode, price, fetchedAt))
                .toList();

        if (rows.isEmpty()) {
            log.warn("Infina returned price history but none fell within window: assetCode={}, fromDate={}, toDate={}, totalFetched={}",
                    assetCode, fromDate, toDate, prices.size());
            return;
        }

        stockPriceHistoryRepository.saveAll(rows);
        log.info("Stock price backfill completed: assetCode={}, rows={}, totalFetched={}", assetCode, rows.size(), prices.size());
    }

    @Override
    public void refreshDay(String assetCode, LocalDate dataDate) {
        List<StockPriceResponse> prices = infinaService.getStockPrices(toEquityCode(assetCode), dataDate.toString());
        if (prices.isEmpty()) {
            log.warn("Infina returned no price for asset on given day: assetCode={}, dataDate={}", assetCode, dataDate);
            return;
        }

        StockPriceResponse price = prices.getFirst();
        Optional<StockPriceHistory> existing =
                stockPriceHistoryRepository.findByAssetCodeAndDataDate(assetCode, dataDate);

        StockPriceHistory row = existing.orElseGet(() -> toEntity(assetCode, price, Instant.now()));
        row.setClosePrice(price.closePrice());
        row.setFetchedAt(Instant.now());

        stockPriceHistoryRepository.save(row);
    }

    @Override
    public void purgeBefore(String assetCode, LocalDate cutoffDate) {
        stockPriceHistoryRepository.deleteByAssetCodeAndDataDateBefore(assetCode, cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockPriceHistory> getWindow(String assetCode, LocalDate fromDate, LocalDate toDate) {
        return stockPriceHistoryRepository
                .findByAssetCodeAndDataDateBetweenOrderByDataDateAsc(assetCode, fromDate, toDate);
    }

    private String toEquityCode(String assetCode) {
        return assetCode.endsWith(EQUITY_SUFFIX) ? assetCode : assetCode + EQUITY_SUFFIX;
    }

    private StockPriceHistory toEntity(String assetCode, StockPriceResponse price, Instant fetchedAt) {
        return StockPriceHistory.builder()
                .assetCode(assetCode)
                .dataDate(price.dataDate())
                .closePrice(price.closePrice())
                .fetchedAt(fetchedAt)
                .build();
    }
}
