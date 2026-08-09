package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationType;

import java.util.Map;

/** Ic motor: EmailService bu arayuzun uzerine ince bir cephedir, Kafka detayi disari sizmaz. */
public interface NotificationService {
    void notify(NotificationCommand command);

    /** Locale'i otomatik cozer (LocaleContextHolder). Tekrar tekrar NotificationCommand olusturma ameleligi kalkar. */
    void notify(NotificationType type, String email, Map<String, String> params);
}
