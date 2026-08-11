package com.akademi.finsight.auth.verificationtoken.scheduler;

import com.akademi.finsight.auth.verificationtoken.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationTokenScheduler {

    private final VerificationTokenService tokenService;

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteExpiredVerificationTokens() {
        int deletedCount = tokenService.deleteExpiredTokens();
        log.info("{} expired verification token deleted.", deletedCount);
    }
}
