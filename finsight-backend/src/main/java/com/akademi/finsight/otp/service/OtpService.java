package com.akademi.finsight.otp.service;

import com.akademi.finsight.otp.model.OtpGenerateResult;
import com.akademi.finsight.otp.model.OtpVerificationResult;

import java.util.Locale;

public interface OtpService {
    OtpGenerateResult generateOtp(String email, Locale locale);

    OtpVerificationResult validateOtp(String email, String inputCode);
}
