package com.akademi.finsight.auth.otp.service;

public interface OtpService {
    void generateOtp(String email, String firstName);

    void validateOtp(String email, String inputCode);

    boolean validateActiveOtp(String email);
}
