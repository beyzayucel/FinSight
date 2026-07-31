package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.model.NotificationRequestedEvent;

/** Olayi kuyruga birakma sozlesmesi; implementasyonu messaging paketinde. */
public interface NotificationEventPublisher {
    void publish(NotificationRequestedEvent event);
}
