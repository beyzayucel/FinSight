package com.akademi.finsight.notification.messaging;

import com.akademi.finsight.notification.config.NotificationProperties;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.service.NotificationEventPublisher;
import com.akademi.finsight.notification.service.NotificationPublishException;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationPublisher implements NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationPublisher.class);

    private final KafkaTemplate<String, NotificationRequestedEvent> kafkaTemplate;
    private final NotificationProperties properties;
    private final MeterRegistry meterRegistry;

    public KafkaNotificationPublisher(
            KafkaTemplate<String, NotificationRequestedEvent> kafkaTemplate,
            NotificationProperties properties,
            MeterRegistry meterRegistry
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    /** Bilerek ack beklenmez, caginin thread'i bloklanmaz; broker erisilemezse yalniz loglanir. */
    @Override
    public void publish(NotificationRequestedEvent event) {
        try {
            kafkaTemplate.send(properties.kafka().topic(), partitionKey(event), event)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            meterRegistry.counter("notification.publish.failed",
                                    "type", event.getType().name()).increment();
                            log.error("Bildirim kuyruga yazilamadi: event={} tip={}",
                                    event.getEventId(), event.getType(), exception);
                        }
                    });
        } catch (RuntimeException exception) {
            throw new NotificationPublishException(event.getEventId(), exception);
        }
    }

    /** userId varsa partition anahtari o, yoksa (VERIFICATION_EMAIL'de oldugu gibi) email. */
    private static String partitionKey(NotificationRequestedEvent event) {
        return event.getUserId() != null ? String.valueOf(event.getUserId()) : event.getEmail();
    }
}
