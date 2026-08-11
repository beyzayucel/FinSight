package com.akademi.finsight.notification.messaging;

import com.akademi.finsight.notification.config.NotificationProperties;
import com.akademi.finsight.notification.exception.NotificationPublishException;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.service.NotificationEventPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotificationPublisher implements NotificationEventPublisher {

    private final KafkaTemplate<String, NotificationRequestedEvent> kafkaTemplate;
    private final NotificationProperties properties;
    private final MeterRegistry meterRegistry;

    /** Bilerek ack beklenmez, caginin thread'i bloklanmaz; broker erisilemezse yalniz loglanir. */
    @Override
    public void publish(NotificationRequestedEvent event) {
        try {
            kafkaTemplate.send(properties.kafka().topic(), partitionKey(event), event)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            meterRegistry.counter("notification.publish.failed",
                                    "type", event.getType().name()).increment();
                            log.error("Failed to publish notification to Kafka: event={} type={}",
                                    event.getEventId(), event.getType(), exception);
                        }
                    });
        } catch (RuntimeException exception) {
            log.error("Failed to publish notification to Kafka synchronously: event={}", event.getEventId(), exception);
            throw new NotificationPublishException(exception);
        }
    }

    private static String partitionKey(NotificationRequestedEvent event) {
        return event.getEmail();
    }
}
