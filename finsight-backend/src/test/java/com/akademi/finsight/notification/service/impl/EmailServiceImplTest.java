package com.akademi.finsight.notification.service.impl;

import com.akademi.finsight.auth.passwordreset.dto.PasswordResetEmailRequest;
import com.akademi.finsight.auth.verificationtoken.dto.VerificationTokenRequest;
import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<NotificationCommand> commandCaptor;

    // ──────────────────────────────────────────────────────────────
    // sendVerificationEmail
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendVerificationEmail")
    class SendVerificationEmail {

        @Test
        @DisplayName("should map all template parameters")
        void sendVerificationEmail_shouldMapAllParameters() {
            emailService.sendVerificationEmail(createVerificationRequest(), Locale.forLanguageTag("en"));

            verify(notificationService).notify(commandCaptor.capture());
            NotificationCommand command = commandCaptor.getValue();
            assertEquals(NotificationType.VERIFICATION_EMAIL, command.getType());
            assertEquals("mehmet@test.com", command.getEmail());
            assertEquals("en", command.getLocale());
            assertEquals("mehmet", command.getParams().get("username"));
            assertEquals("Gecici123!", command.getParams().get("temporaryPassword"));
            assertEquals("https://finsight.local/verify?token=abc", command.getParams().get("verificationUrl"));
        }

        @Test
        @DisplayName("should fall back to default locale when locale is null")
        void sendVerificationEmail_shouldFallBackToDefaultLocale() {
            emailService.sendVerificationEmail(createVerificationRequest(), null);

            verify(notificationService).notify(commandCaptor.capture());
            assertEquals("tr", commandCaptor.getValue().getLocale());
        }

        /** Gecici sifre ve token loglara sizmamali; NotificationCommand bunlari toString'den haric tutar. */
        @Test
        @DisplayName("should not leak sensitive parameters in toString")
        void sendVerificationEmail_shouldNotLeakSensitiveParameters() {
            emailService.sendVerificationEmail(createVerificationRequest(), Locale.forLanguageTag("tr"));

            verify(notificationService).notify(commandCaptor.capture());
            String asText = commandCaptor.getValue().toString();
            assertFalse(asText.contains("Gecici123!"));
            assertFalse(asText.contains("token=abc"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // sendPasswordResetEmail
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendPasswordResetEmail")
    class SendPasswordResetEmail {

        @Test
        @DisplayName("should map all template parameters")
        void sendPasswordResetEmail_shouldMapAllParameters() {
            PasswordResetEmailRequest request = new PasswordResetEmailRequest(
                    "Mehmet", "mehmet@test.com", "https://finsight.local/reset?token=xyz");

            emailService.sendPasswordResetEmail(request, Locale.forLanguageTag("tr"));

            verify(notificationService).notify(commandCaptor.capture());
            NotificationCommand command = commandCaptor.getValue();
            assertEquals(NotificationType.PASSWORD_RESET_EMAIL, command.getType());
            assertEquals("tr", command.getLocale());
            assertEquals("Mehmet", command.getParams().get("firstName"));
            assertEquals("https://finsight.local/reset?token=xyz", command.getParams().get("resetUrl"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private VerificationTokenRequest createVerificationRequest() {
        return new VerificationTokenRequest(
                "mehmet", "mehmet@test.com", "Gecici123!", "https://finsight.local/verify?token=abc");
    }
}
