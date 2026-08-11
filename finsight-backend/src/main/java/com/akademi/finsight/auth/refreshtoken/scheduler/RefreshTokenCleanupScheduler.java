package com.akademi.finsight.auth.refreshtoken.scheduler;

import com.akademi.finsight.auth.refreshtoken.repository.RefreshTokenRepository;
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
public class RefreshTokenCleanupScheduler {

    private static final long CLEANUP_THRESHOLD_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void revokeExpiredTokens() {
        int count = refreshTokenRepository.bulkRevokeExpired(Instant.now());
        log.info("Token cleanup completed: event=REVOKE_EXPIRED, count={}", count);
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void deleteOldRevokedTokens() {
        Instant threshold = Instant.now().minus(CLEANUP_THRESHOLD_DAYS, ChronoUnit.DAYS);
        int count = refreshTokenRepository.deleteOldRevoked(threshold);
        log.info("Token cleanup completed: event=DELETE_OLD_REVOKED, count={}", count);
    }
}
