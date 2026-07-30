package com.akademi.finsight.auth.verificationtoken.dto;

public record VerificationTokenRequest(
        String username,
        String email,
        String temporaryPassword,
        String verificationUrl
) {
}
