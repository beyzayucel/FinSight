package com.akademi.finsight.notification.model;

/**
 * Bildirim tipleri. Her tip yalnizca sablon anahtarini tasir.
 */
public enum NotificationType {
    VERIFICATION_EMAIL("notification.verificationEmail"),
    OTP_EMAIL("notification.otpEmail");

    private final String templateKey;

    NotificationType(String templateKey) {
        this.templateKey = templateKey;
    }

    public String templateKey() {
        return templateKey;
    }
}
