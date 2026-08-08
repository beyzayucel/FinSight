package com.akademi.finsight.notification.support;

import com.akademi.finsight.notification.config.NotificationProperties;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.model.NotificationType;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/** Testlerde tekrar eden olay ve ayar nesnelerini tek yerden uretir. */
public final class NotificationFixtures {

    public static final String EMAIL = "mehmet@test.com";
    public static final String EVENT_ID = "11111111-1111-1111-1111-111111111111";
    public static final String TOPIC = "finsight.notification.requested.v1";

    private NotificationFixtures() {
    }

    public static NotificationRequestedEvent event() {
        return event(NotificationType.VERIFICATION_EMAIL, Map.of("username", "mehmet"), "tr");
    }

    public static NotificationRequestedEvent event(NotificationType type, Map<String, String> params, String locale) {
        return new NotificationRequestedEvent(EVENT_ID, type, EMAIL, params, locale, Instant.parse("2026-01-01T00:00:00Z"));
    }

    public static NotificationRequestedEvent eventWithEmail(String email) {
        return new NotificationRequestedEvent(EVENT_ID, NotificationType.VERIFICATION_EMAIL, email,
                Map.of(), "tr", Instant.parse("2026-01-01T00:00:00Z"));
    }

    public static NotificationProperties properties() {
        return new NotificationProperties(kafka(), new NotificationProperties.Idempotency(Duration.ofHours(24)), "tr");
    }

    public static NotificationProperties.Kafka kafka() {
        return new NotificationProperties.Kafka(
                TOPIC,
                "notification-service",
                Duration.ofDays(30),
                ".DLT",
                3,
                1,
                new NotificationProperties.Kafka.Retry(3, Duration.ofSeconds(1), 2.0, Duration.ofSeconds(4))
        );
    }
}
