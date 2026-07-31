package com.akademi.finsight.otp.model;

import java.time.Duration;

public record OtpGenerateResult(
        boolean success,
        String message,
        Long remainingCooldownSeconds
) {
}
