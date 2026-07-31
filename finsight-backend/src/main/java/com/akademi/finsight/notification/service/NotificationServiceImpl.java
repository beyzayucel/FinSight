package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationEventPublisher publisher;

    public NotificationServiceImpl(NotificationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void notify(NotificationCommand command) {
        NotificationRequestedEvent event = new NotificationRequestedEvent(
                UUID.randomUUID().toString(),
                command.getType(),
                command.getUserId(),
                command.getEmail(),
                command.getParams(),
                command.getLocale(),
                Instant.now()
        );
        publisher.publish(event);
    }
}
