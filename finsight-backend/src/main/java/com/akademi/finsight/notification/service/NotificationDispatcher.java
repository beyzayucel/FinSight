package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.mail.EmailNotificationSender;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.repository.IdempotencyStore;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Ince orkestrator: idempotency -> render -> gonder -> metrik. */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final EmailNotificationSender sender;
    private final NotificationRenderer renderer;
    private final IdempotencyStore idempotencyStore;
    private final MeterRegistry meterRegistry;

    public void dispatch(NotificationRequestedEvent event) {
        if (!idempotencyStore.tryAcquire(event.getEventId())) {
            log.info("Event already processed, skipping: event={}", event.getEventId());
            return;
        }
        try {
            sender.send(renderer.render(event));
            meterRegistry.counter("notification.sent", "type", event.getType().name()).increment();
            log.info("Notification sent: event={} type={}", event.getEventId(), event.getType());
        } catch (RuntimeException exception) {
            idempotencyStore.release(event.getEventId());
            meterRegistry.counter("notification.failed",
                    "type", event.getType().name(),
                    "reason", exception.getClass().getSimpleName()).increment();
            log.error("Failed to send notification: event={} type={}", event.getEventId(), event.getType(), exception);
            throw exception;
        }
    }
}
