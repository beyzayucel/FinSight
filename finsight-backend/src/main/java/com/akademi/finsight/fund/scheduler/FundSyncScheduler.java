package com.akademi.finsight.fund.scheduler;

import com.akademi.finsight.fund.service.FundSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundSyncScheduler {

    private static final String SCHEDULED_TRIGGER = "SCHEDULED";
    private static final String STARTUP_TRIGGER = "STARTUP";

    private final FundSyncService fundSyncService;

    @Scheduled(cron = "${fund.sync.cron}", zone = "${fund.sync.zone}")
    public void syncHourly() {
        runSync(SCHEDULED_TRIGGER);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncAtStartup() {
        runSync(STARTUP_TRIGGER);
    }

    private void runSync(String trigger) {
        log.info("Fund sync triggered: trigger={}", trigger);
        try {
            fundSyncService.sync();
        } catch (Exception e) {
            log.warn("Fund sync failed: event=FUND_SYNC_FAILED, trigger={}; application will continue with the "
                    + "previously synced data.", trigger, e);
        }
    }
}
