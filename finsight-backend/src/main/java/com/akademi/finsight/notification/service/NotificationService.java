package com.akademi.finsight.notification.service;

import com.akademi.finsight.notification.model.NotificationCommand;
import com.akademi.finsight.notification.model.NotificationType;

import java.util.Map;

/** Is katmaninin tek bildirim giris noktasi; Kafka ve kanal detaylari disari sizmaz. */
public interface NotificationService {
    void notify(NotificationCommand command);

    /** Locale'i otomatik cozer (LocaleContextHolder). Tekrar tekrar NotificationCommand olusturma ameleligi kalkar. */
    void notify(NotificationType type, String email, Map<String, String> params);
}
