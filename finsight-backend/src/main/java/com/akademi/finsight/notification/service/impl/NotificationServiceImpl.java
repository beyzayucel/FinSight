package com.akademi.finsight.notification.service.impl;

import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationRequestedEvent;
import com.akademi.finsight.notification.service.NotificationEventPublisher;
import com.akademi.finsight.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationEventPublisher publisher;

    @Override
    public void notify(NotificationCommand command) {
        NotificationRequestedEvent event = new NotificationRequestedEvent(
                UUID.randomUUID().toString(),
                command.getType(),
                command.getEmail(),
                command.getParams(),
                command.getLocale(),
                Instant.now()
        );
        publisher.publish(event);
    }
}
