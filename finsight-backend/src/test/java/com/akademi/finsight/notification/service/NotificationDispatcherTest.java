package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.exception.EmailSendingException;
import com.akademi.finsight.notification.mail.EmailNotificationSender;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.model.RenderedNotification;
import com.akademi.finsight.notification.repository.IdempotencyStore;
import com.akademi.finsight.notification.support.NotificationFixtures;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Mock
    private EmailNotificationSender sender;

    @Mock
    private NotificationRenderer renderer;

    @Mock
    private IdempotencyStore idempotencyStore;

    private MeterRegistry meterRegistry;
    private NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        dispatcher = new NotificationDispatcher(sender, renderer, idempotencyStore, meterRegistry);
    }

    // ──────────────────────────────────────────────────────────────
    // dispatch
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("dispatch")
    class Dispatch {

        @Test
        @DisplayName("should render, send and increment sent counter when key acquired")
        void dispatch_shouldSendWhenIdempotencyKeyAcquired() {
            NotificationRequestedEvent event = NotificationFixtures.event();
            RenderedNotification rendered = renderedNotification();
            when(idempotencyStore.tryAcquire(NotificationFixtures.EVENT_ID)).thenReturn(true);
            when(renderer.render(event)).thenReturn(rendered);

            dispatcher.dispatch(event);

            verify(sender).send(rendered);
            assertEquals(1.0, counterCount("notification.sent", "type", "VERIFICATION_EMAIL"));
        }

        @Test
        @DisplayName("should skip render and send when event already processed")
        void dispatch_shouldSkipAlreadyProcessedEvent() {
            NotificationRequestedEvent event = NotificationFixtures.event();
            when(idempotencyStore.tryAcquire(NotificationFixtures.EVENT_ID)).thenReturn(false);

            dispatcher.dispatch(event);

            verifyNoInteractions(renderer, sender);
            assertNull(meterRegistry.find("notification.sent").counter());
        }

        @Test
        @DisplayName("should release key, count failure and rethrow when send fails")
        void dispatch_shouldReleaseKeyAndRethrowWhenSendFails() {
            NotificationRequestedEvent event = NotificationFixtures.event();
            RenderedNotification rendered = renderedNotification();
            when(idempotencyStore.tryAcquire(NotificationFixtures.EVENT_ID)).thenReturn(true);
            when(renderer.render(event)).thenReturn(rendered);
            doThrow(new EmailSendingException(new RuntimeException("smtp down"))).when(sender).send(rendered);

            assertThrows(EmailSendingException.class, () -> dispatcher.dispatch(event));

            verify(idempotencyStore).release(NotificationFixtures.EVENT_ID);
            assertEquals(1.0, meterRegistry.get("notification.failed")
                    .tag("type", "VERIFICATION_EMAIL")
                    .tag("reason", "EmailSendingException")
                    .counter().count());
            assertNull(meterRegistry.find("notification.sent").counter());
        }

        /** Anahtar birakilmazsa olay TTL boyunca (24 saat) kalici olarak bloke olur. */
        @Test
        @DisplayName("should release key when render fails")
        void dispatch_shouldReleaseKeyWhenRenderFails() {
            NotificationRequestedEvent event = NotificationFixtures.event();
            when(idempotencyStore.tryAcquire(NotificationFixtures.EVENT_ID)).thenReturn(true);
            when(renderer.render(event)).thenThrow(new IllegalStateException("render patladi"));

            assertThrows(IllegalStateException.class, () -> dispatcher.dispatch(event));

            verify(idempotencyStore).release(NotificationFixtures.EVENT_ID);
            verifyNoInteractions(sender);
            assertEquals(1.0, counterCount("notification.failed", "reason", "IllegalStateException"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private RenderedNotification renderedNotification() {
        return new RenderedNotification(NotificationFixtures.EMAIL, "konu", "govde");
    }

    private double counterCount(String name, String tagKey, String tagValue) {
        return meterRegistry.get(name).tag(tagKey, tagValue).counter().count();
    }
}
