package com.akademi.finsight.ai.model.scheduler;

import com.akademi.finsight.ai.model.service.ModelDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModelDataSyncScheduler {

    private static final String SCHEDULED_TRIGGER = "SCHEDULED";
    private static final String STARTUP_TRIGGER = "STARTUP";

    private final ModelDataSyncService modelDataSyncService;

    @Scheduled(cron = "${market.sync.cron}", zone = "${market.sync.zone}")
    public void syncHourly() {
        runSync(SCHEDULED_TRIGGER);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncAtStartup() {
        runSync(STARTUP_TRIGGER);
    }

    private void runSync(String trigger) {
        log.info("Model data sync triggered: trigger={}", trigger);
        try {
            modelDataSyncService.sync();
        } catch (Exception e) {
            log.warn("Model data sync failed: event=MODEL_DATA_SYNC_FAILED, trigger={}; application will continue with "
                    + "previously synced data.", trigger, e);
        }
    }
}
