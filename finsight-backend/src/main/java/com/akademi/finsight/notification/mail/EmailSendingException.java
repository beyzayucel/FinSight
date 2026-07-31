package com.akademi.finsight.notification.mail;

/** Retry edilebilir sayilir - KafkaConfiguration'in "retry etme" listesine girmez. */
public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
