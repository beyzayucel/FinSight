package com.akademi.finsight.auth.passwordreset.scheduler;

import com.akademi.finsight.auth.passwordreset.service.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetTokenScheduler {

    private final PasswordResetTokenService tokenService;

    @Scheduled(cron = "0 30 2 * * *")
    public void deleteExpiredPasswordResetTokens() {
        int deletedCount = tokenService.deleteExpiredTokens();
        log.info("{} expired password reset token deleted.", deletedCount);
    }
}
