package com.akademi.finsight.notification.service;

/** Kalici hata (veri eksik/hatali) - retry edilmez, direkt DLT'ye gider. */
public class InvalidNotificationException extends RuntimeException {
    public InvalidNotificationException(String message) {
        super(message);
    }
}
