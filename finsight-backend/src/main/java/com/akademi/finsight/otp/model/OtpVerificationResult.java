package com.akademi.finsight.otp.model;

public record OtpVerificationResult(
        boolean success,
        String message
) {}
