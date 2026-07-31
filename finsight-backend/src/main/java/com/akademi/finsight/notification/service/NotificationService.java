package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.model.NotificationCommand;

/** Ic motor: EmailService bu arayuzun uzerine ince bir cephedir, Kafka detayi disari sizmaz. */
public interface NotificationService {
    void notify(NotificationCommand command);
}
