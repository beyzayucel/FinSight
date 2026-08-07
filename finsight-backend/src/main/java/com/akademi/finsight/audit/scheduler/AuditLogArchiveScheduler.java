package com.akademi.finsight.audit.scheduler;

import com.akademi.finsight.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogArchiveScheduler {

    private static final int ARCHIVE_AFTER_DAYS = 90;

    private final AuditLogRepository auditLogRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void archiveOldAuditLogs() {
        Instant cutoff = Instant.now().minus(ARCHIVE_AFTER_DAYS, ChronoUnit.DAYS);
        int archived = auditLogRepository.archiveOlderThan(cutoff, Instant.now());

        if (archived > 0) {
            log.info("Audit log archive completed: event=AUDIT_LOG_ARCHIVED, count={}", archived);
        }
    }
}
