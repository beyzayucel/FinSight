package com.akademi.finsight.fund.stockprice.service;

import com.akademi.finsight.fund.stockprice.entity.StockPriceHistory;

import java.time.LocalDate;
import java.util.List;

public interface StockPriceService {


    void backfillIfMissing(String assetCode, LocalDate fromDate, LocalDate toDate);

    void refreshDay(String assetCode, LocalDate dataDate);

    void purgeBefore(String assetCode, LocalDate cutoffDate);

    List<StockPriceHistory> getWindow(String assetCode, LocalDate fromDate, LocalDate toDate);
}
