package com.akademi.finsight.notification.messaging;

import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.service.NotificationDispatcher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Bilerek ince: hata yonetimi KafkaConfiguration'daki DefaultErrorHandler'da merkezidir. */
@Component
public class NotificationKafkaListener {

    private final NotificationDispatcher dispatcher;

    public NotificationKafkaListener(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @KafkaListener(
            topics = "${notification.kafka.topic}",
            groupId = "${notification.kafka.group-id}"
    )
    public void consume(NotificationRequestedEvent event) {
        dispatcher.dispatch(event);
    }
}
