package com.akademi.finsight.notification.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/** NotificationService cagrisinin parametre nesnesi; Kafka'ya gitmez, yalnizca modul ici kullanilir. */
@Getter
@ToString
@EqualsAndHashCode
public class NotificationCommand {

    private final NotificationType type;
    private final String email;
    // gecici sifre/token tasiyabilir - toString'e (dolayisiyla loglara) sizmasin
    @ToString.Exclude
    private final Map<String, String> params;
    private final String locale;

    public NotificationCommand(NotificationType type, String email,
                                Map<String, String> params, String locale) {
        this.type = type;
        this.email = email;
        this.params = params == null ? Map.of() : Map.copyOf(params);
        this.locale = (locale == null || locale.isBlank()) ? "tr" : locale;
    }
}
