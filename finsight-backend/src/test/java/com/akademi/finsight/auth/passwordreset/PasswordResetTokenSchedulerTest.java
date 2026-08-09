package com.akademi.finsight.auth.passwordreset;

import com.akademi.finsight.auth.passwordreset.scheduler.PasswordResetTokenScheduler;
import com.akademi.finsight.auth.passwordreset.service.PasswordResetTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenSchedulerTest {

    @Mock
    private PasswordResetTokenService tokenService;

    @InjectMocks
    private PasswordResetTokenScheduler scheduler;

    // ──────────────────────────────────────────────────────────────
    // deleteExpiredPasswordResetTokens
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteExpiredPasswordResetTokens")
    class DeleteExpiredPasswordResetTokens {

        @Test
        @DisplayName("should delegate cleanup to the token service")
        void shouldDelegateToTokenService() {
            when(tokenService.deleteExpiredTokens()).thenReturn(3);

            assertDoesNotThrow(() -> scheduler.deleteExpiredPasswordResetTokens());

            verify(tokenService).deleteExpiredTokens();
        }
    }
}
