package com.akademi.finsight.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Modulun {@code notification.*} ayarlari. {@code notification.mail} blogu
 * {@link com.akademi.finsight.notification.mail.MailProperties} tarafindan baglanir.
 */
@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
        @DefaultValue Kafka kafka,
        @DefaultValue Idempotency idempotency,
        @DefaultValue("tr") String defaultLocale
) {

    public record Kafka(
            @DefaultValue("finsight.notification.requested.v1") String topic,
            @DefaultValue("notification-service") String groupId,
            @DefaultValue("30d") Duration deadLetterRetention,
            @DefaultValue(".DLT") String deadLetterSuffix,
            @DefaultValue("3") int partitions,
            @DefaultValue("1") int replicas,
            @DefaultValue Retry retry
    ) {
        public record Retry(
                @DefaultValue("3") int maxAttempts,
                @DefaultValue("1s") Duration initialInterval,
                @DefaultValue("2") double multiplier,
                @DefaultValue("4s") Duration maxInterval
        ) {
        }
    }

    public record Idempotency(
            @DefaultValue("24h") Duration ttl
    ) {
    }
}
