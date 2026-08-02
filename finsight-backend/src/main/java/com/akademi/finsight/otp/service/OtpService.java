package com.akademi.finsight.otp.service;

import java.util.Locale;

public interface OtpService {
    void generateOtp(String email, String firstName, Locale locale);

    void validateOtp(String email, String inputCode);

    boolean validateActiveOtp(String email);
}
