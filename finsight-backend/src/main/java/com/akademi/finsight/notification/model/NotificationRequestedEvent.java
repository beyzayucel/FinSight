package com.akademi.finsight.notification.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Kafka'da JSON olarak tasinan mesaj sozlesmesi; dil de (locale) event icinde tasinir. */
@Getter
@ToString
@EqualsAndHashCode
public class NotificationRequestedEvent {

    private final String eventId;
    private final NotificationType type;
    private final UUID userId;
    private final String email;
    // gecici sifre/token tasiyabilir - toString'e (dolayisiyla loglara) sizmasin
    @ToString.Exclude
    private final Map<String, String> params;
    private final String locale;
    private final Instant occurredAt;

    @JsonCreator
    public NotificationRequestedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("type") NotificationType type,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("email") String email,
            @JsonProperty("params") Map<String, String> params,
            @JsonProperty("locale") String locale,
            @JsonProperty("occurredAt") Instant occurredAt
    ) {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        this.eventId = eventId;
        this.type = type;
        this.userId = userId;
        this.email = email;
        this.params = params == null ? Map.of() : Map.copyOf(params);
        this.locale = locale == null || locale.isBlank() ? "tr" : locale;
        this.occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }
}
