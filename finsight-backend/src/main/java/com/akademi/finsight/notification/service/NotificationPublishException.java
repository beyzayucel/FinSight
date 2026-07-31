package com.akademi.finsight.notification.service;

/** Olay kuyruga yazilamadi - yalniz send() senkron patlarsa yukselir, ack timeout'ta degil. */
public class NotificationPublishException extends RuntimeException {
    public NotificationPublishException(String eventId, Throwable cause) {
        super("Bildirim kuyruga yazilamadi: " + eventId, cause);
    }
}
