package com.akademi.finsight.notification.service.impl;

import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.model.NotificationType;
import com.akademi.finsight.notification.service.NotificationEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationEventPublisher publisher;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Captor
    private ArgumentCaptor<NotificationRequestedEvent> eventCaptor;

    // ──────────────────────────────────────────────────────────────
    // notify
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("notify")
    class Notify {

        @Test
        @DisplayName("should build event from command with generated id and timestamp")
        void notify_shouldBuildEventFromCommand() {
            Instant before = Instant.now();
            NotificationCommand command = new NotificationCommand(
                    NotificationType.PASSWORD_RESET_EMAIL,
                    "mehmet@test.com",
                    Map.of("resetUrl", "https://finsight.local/reset?token=abc"),
                    "en");

            notificationService.notify(command);

            verify(publisher).publish(eventCaptor.capture());
            NotificationRequestedEvent event = eventCaptor.getValue();
            assertEquals(NotificationType.PASSWORD_RESET_EMAIL, event.getType());
            assertEquals("mehmet@test.com", event.getEmail());
            assertEquals("https://finsight.local/reset?token=abc", event.getParams().get("resetUrl"));
            assertEquals("en", event.getLocale());
            assertFalse(event.getOccurredAt().isBefore(before));
            assertDoesNotThrow(() -> UUID.fromString(event.getEventId()));
        }

        /** Ayni eventId ile ikinci mail idempotency tarafindan yutulurdu. */
        @Test
        @DisplayName("should generate a unique event id per call")
        void notify_shouldGenerateUniqueEventIdPerCall() {
            NotificationCommand command = new NotificationCommand(
                    NotificationType.VERIFICATION_EMAIL, "mehmet@test.com", Map.of(), "tr");

            notificationService.notify(command);
            notificationService.notify(command);

            verify(publisher, times(2)).publish(eventCaptor.capture());
            assertNotEquals(
                    eventCaptor.getAllValues().get(0).getEventId(),
                    eventCaptor.getAllValues().get(1).getEventId());
        }
    }
}
