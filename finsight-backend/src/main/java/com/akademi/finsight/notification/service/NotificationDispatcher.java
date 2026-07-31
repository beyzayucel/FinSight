package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.mail.EmailNotificationSender;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.repository.IdempotencyStore;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Ince orkestrator: idempotency -> render -> gonder -> metrik.
 */
@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final EmailNotificationSender sender;
    private final NotificationRenderer renderer;
    private final IdempotencyStore idempotencyStore;
    private final MeterRegistry meterRegistry;

    public NotificationDispatcher(
            EmailNotificationSender sender,
            NotificationRenderer renderer,
            IdempotencyStore idempotencyStore,
            MeterRegistry meterRegistry
    ) {
        this.sender = sender;
        this.renderer = renderer;
        this.idempotencyStore = idempotencyStore;
        this.meterRegistry = meterRegistry;
    }

    public void dispatch(NotificationRequestedEvent event) {
        if (!idempotencyStore.tryAcquire(event.getEventId())) {
            log.info("Olay zaten islenmis, atlandi: event={}", event.getEventId());
            return;
        }
        try {
            sender.send(renderer.render(event));
            meterRegistry.counter("notification.sent", "type", event.getType().name()).increment();
            log.info("Bildirim gonderildi: event={} tip={}", event.getEventId(), event.getType());
        } catch (RuntimeException exception) {
            idempotencyStore.release(event.getEventId());
            meterRegistry.counter("notification.failed",
                    "type", event.getType().name(),
                    "reason", exception.getClass().getSimpleName()).increment();
            log.error("Bildirim gonderilemedi: event={} tip={}", event.getEventId(), event.getType(), exception);
            throw exception;
        }
    }
}
