package com.akademi.finsight.fund.scheduler;

import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.constant.FundStockAllocationConstants;
import com.akademi.finsight.fund.dto.response.FundStockWeightResponse;
import com.akademi.finsight.fund.service.FundStockAllocationService;
import com.akademi.finsight.fund.stockprice.service.StockPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceRefreshScheduler {

    private static final String SCHEDULED_TRIGGER = "SCHEDULED";
    private static final String STARTUP_TRIGGER = "STARTUP";

    private final FundStockAllocationService fundStockAllocationService;
    private final StockPriceService stockPriceService;
    private final FundProperties fundProperties;

    @Value("${stockprice.history.window-days:90}")
    private int historyWindowDays;

    @Scheduled(cron = "${stockprice.refresh.cron}", zone = "${stockprice.refresh.zone}")
    public void refreshDaily() {
        runRefresh(SCHEDULED_TRIGGER);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void refreshAtStartup() {
        runRefresh(STARTUP_TRIGGER);
    }

    private void runRefresh(String trigger) {
        String fundCode = fundProperties.getCode();

        List<String> assetCodes = fundStockAllocationService.getBreakdownByFundCode(fundCode, null)
                .items().stream()
                .map(FundStockWeightResponse::assetCode)
                .filter(code -> !FundStockAllocationConstants.OTHERS_ASSET_CODE.equals(code))
                .toList();

        int dataLagDays = fundProperties.getSync().getDataLagDays();
        LocalDate today = LocalDate.now();
        LocalDate toDate = dataLagDays > 0 ? today.minusDays(dataLagDays) : today;
        LocalDate fromDate = toDate.minusDays(historyWindowDays);

        log.info("Stock price refresh triggered: trigger={}, fundCode={}, assetCount={}, window=[{}, {}]",
                trigger, fundCode, assetCodes.size(), fromDate, toDate);

        int failureCount = 0;
        for (String assetCode : assetCodes) {
            try {
                stockPriceService.backfillIfMissing(assetCode, fromDate, toDate);
                stockPriceService.refreshDay(assetCode, toDate);
                stockPriceService.purgeBefore(assetCode, fromDate);
            } catch (Exception e) {
                failureCount++;
                log.warn("Stock price refresh failed for asset: event=STOCK_PRICE_REFRESH_FAILED, assetCode={}",
                        assetCode, e);
            }
        }

        log.info("Stock price refresh completed: trigger={}, fundCode={}, succeeded={}, failed={}",
                trigger, fundCode, assetCodes.size() - failureCount, failureCount);
    }
}
