package com.akademi.finsight.fund.stockprice.service.impl;

import com.akademi.finsight.fund.stockprice.entity.StockPriceHistory;
import com.akademi.finsight.fund.stockprice.repository.StockPriceHistoryRepository;
import com.akademi.finsight.integration.infina.dto.response.stock.StockPriceResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockPriceServiceImpl Tests")
class StockPriceServiceImplTest {

    @Mock
    private StockPriceHistoryRepository stockPriceHistoryRepository;

    @Mock
    private InfinaService infinaService;

    @InjectMocks
    private StockPriceServiceImpl stockPriceService;

    private static final String ASSET_CODE = "THYAO";
    private static final LocalDate FROM_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2026, 1, 31);

    @Nested
    @DisplayName("backfillIfMissing")
    class BackfillIfMissing {

        @Test
        @DisplayName("should skip entirely when asset already has history")
        void shouldSkipWhenAlreadyExists() {
            when(stockPriceHistoryRepository.existsByAssetCode(ASSET_CODE)).thenReturn(true);

            stockPriceService.backfillIfMissing(ASSET_CODE, FROM_DATE, TO_DATE);

            verifyNoInteractions(infinaService);
            verify(stockPriceHistoryRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("should fetch, filter by window and save rows when history is missing")
        void shouldBackfillWithinWindow() {
            when(stockPriceHistoryRepository.existsByAssetCode(ASSET_CODE)).thenReturn(false);

            List<StockPriceResponse> prices = List.of(
                    new StockPriceResponse(ASSET_CODE, BigDecimal.valueOf(100), LocalDate.of(2025, 12, 31)),
                    new StockPriceResponse(ASSET_CODE, BigDecimal.valueOf(102), LocalDate.of(2026, 1, 10)),
                    new StockPriceResponse(ASSET_CODE, BigDecimal.valueOf(105), LocalDate.of(2026, 1, 20)),
                    new StockPriceResponse(ASSET_CODE, BigDecimal.valueOf(110), LocalDate.of(2026, 2, 5))
            );
            when(infinaService.getStockPrices(ASSET_CODE + ".E", null)).thenReturn(prices);

            stockPriceService.backfillIfMissing(ASSET_CODE, FROM_DATE, TO_DATE);

            ArgumentCaptor<List<StockPriceHistory>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockPriceHistoryRepository).saveAll(captor.capture());

            List<StockPriceHistory> saved = captor.getValue();
            assertEquals(2, saved.size());
            assertTrue(saved.stream().allMatch(row -> !row.getDataDate().isBefore(FROM_DATE)
                    && !row.getDataDate().isAfter(TO_DATE)));
            assertEquals(ASSET_CODE, saved.get(0).getAssetCode());
        }

        @Test
        @DisplayName("should append .E suffix only when asset code doesn't already have it")
        void shouldNotDoubleAppendEquitySuffix() {
            when(stockPriceHistoryRepository.existsByAssetCode("THYAO.E")).thenReturn(false);
            when(infinaService.getStockPrices("THYAO.E", null)).thenReturn(List.of());

            stockPriceService.backfillIfMissing("THYAO.E", FROM_DATE, TO_DATE);

            verify(infinaService).getStockPrices("THYAO.E", null);
        }

        @Test
        @DisplayName("should do nothing when Infina returns no price history")
        void shouldSkipWhenInfinaReturnsEmpty() {
            when(stockPriceHistoryRepository.existsByAssetCode(ASSET_CODE)).thenReturn(false);
            when(infinaService.getStockPrices(ASSET_CODE + ".E", null)).thenReturn(List.of());

            stockPriceService.backfillIfMissing(ASSET_CODE, FROM_DATE, TO_DATE);

            verify(stockPriceHistoryRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("should do nothing when Infina returns data but none falls within window")
        void shouldSkipWhenNoRowsWithinWindow() {
            when(stockPriceHistoryRepository.existsByAssetCode(ASSET_CODE)).thenReturn(false);
            when(infinaService.getStockPrices(ASSET_CODE + ".E", null)).thenReturn(List.of(
                    new StockPriceResponse(ASSET_CODE, BigDecimal.valueOf(100), LocalDate.of(2025, 6, 1))
            ));

            stockPriceService.backfillIfMissing(ASSET_CODE, FROM_DATE, TO_DATE);

            verify(stockPriceHistoryRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("refreshDay")
    class RefreshDay {

        @Test
        @DisplayName("should update existing row's close price when one exists for the day")
        void shouldUpdateExistingRow() {
            LocalDate dataDate = LocalDate.of(2026, 1, 15);
            StockPriceHistory existing = StockPriceHistory.builder()
                    .assetCode(ASSET_CODE)
                    .dataDate(dataDate)
                    .closePrice(BigDecimal.valueOf(90))
                    .build();

            when(infinaService.getStockPrices(ASSET_CODE + ".E", dataDate.toString()))
                    .thenReturn(List.of(new StockPriceResponse(ASSET_CODE, BigDecimal.valueOf(95), dataDate)));
            when(stockPriceHistoryRepository.findByAssetCodeAndDataDate(ASSET_CODE, dataDate))
                    .thenReturn(Optional.of(existing));

            stockPriceService.refreshDay(ASSET_CODE, dataDate);

            ArgumentCaptor<StockPriceHistory> captor = ArgumentCaptor.forClass(StockPriceHistory.class);
            verify(stockPriceHistoryRepository).save(captor.capture());
            assertEquals(BigDecimal.valueOf(95), captor.getValue().getClosePrice());
        }

        @Test
        @DisplayName("should create a new row when none exists for the day")
        void shouldCreateNewRowWhenMissing() {
            LocalDate dataDate = LocalDate.of(2026, 1, 15);

            when(infinaService.getStockPrices(ASSET_CODE + ".E", dataDate.toString()))
                    .thenReturn(List.of(new StockPriceResponse(ASSET_CODE, BigDecimal.valueOf(95), dataDate)));
            when(stockPriceHistoryRepository.findByAssetCodeAndDataDate(ASSET_CODE, dataDate))
                    .thenReturn(Optional.empty());

            stockPriceService.refreshDay(ASSET_CODE, dataDate);

            ArgumentCaptor<StockPriceHistory> captor = ArgumentCaptor.forClass(StockPriceHistory.class);
            verify(stockPriceHistoryRepository).save(captor.capture());

            StockPriceHistory saved = captor.getValue();
            assertEquals(ASSET_CODE, saved.getAssetCode());
            assertEquals(dataDate, saved.getDataDate());
            assertEquals(BigDecimal.valueOf(95), saved.getClosePrice());
        }

        @Test
        @DisplayName("should do nothing when Infina returns no price for the day")
        void shouldSkipWhenInfinaReturnsEmpty() {
            LocalDate dataDate = LocalDate.of(2026, 1, 15);
            when(infinaService.getStockPrices(ASSET_CODE + ".E", dataDate.toString())).thenReturn(List.of());

            stockPriceService.refreshDay(ASSET_CODE, dataDate);

            verify(stockPriceHistoryRepository, never()).save(any());
            verify(stockPriceHistoryRepository, never()).findByAssetCodeAndDataDate(anyString(), any());
        }
    }

    @Nested
    @DisplayName("purgeBefore")
    class PurgeBefore {

        @Test
        @DisplayName("should delegate deletion to repository with given cutoff date")
        void shouldDelegateToRepository() {
            stockPriceService.purgeBefore(ASSET_CODE, FROM_DATE);

            verify(stockPriceHistoryRepository).deleteByAssetCodeAndDataDateBefore(ASSET_CODE, FROM_DATE);
        }
    }

    @Nested
    @DisplayName("getWindow")
    class GetWindow {

        @Test
        @DisplayName("should return the repository's ordered window result")
        void shouldReturnRepositoryResult() {
            List<StockPriceHistory> expected = List.of(
                    StockPriceHistory.builder().assetCode(ASSET_CODE).dataDate(FROM_DATE).closePrice(BigDecimal.TEN).build()
            );
            when(stockPriceHistoryRepository.findByAssetCodeAndDataDateBetweenOrderByDataDateAsc(
                    ASSET_CODE, FROM_DATE, TO_DATE)).thenReturn(expected);

            List<StockPriceHistory> result = stockPriceService.getWindow(ASSET_CODE, FROM_DATE, TO_DATE);

            assertEquals(expected, result);
        }
    }
}
