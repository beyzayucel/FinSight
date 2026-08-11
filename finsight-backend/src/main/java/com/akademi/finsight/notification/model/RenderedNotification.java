package com.akademi.finsight.notification.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Sablon doldurulduktan sonra gonderime hazir paket.
 */
@Getter
@ToString
@EqualsAndHashCode
public class RenderedNotification {

    private final String destination;
    private final String subject;
    private final String body;

    public RenderedNotification(String destination, String subject, String body) {
        this.destination = destination;
        this.subject = subject;
        this.body = body;
    }
}
