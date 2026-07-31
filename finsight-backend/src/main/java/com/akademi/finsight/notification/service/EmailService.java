package com.akademi.finsight.notification.service;

import com.akademi.finsight.auth.verificationtoken.dto.VerificationTokenRequest;

import java.util.Locale;

/** Disariya acik sozlesme: cagiran (auth) Kafka'yi/topic'i/olay semasini bilmez. */
public interface EmailService {

    /** Kafka ack'ini beklemez, ana istek akisini bloklamaz; gonderim consumer'da asenkron yapilir. */
    void sendVerificationEmail(VerificationTokenRequest request, Locale locale);
}
