package com.akademi.finsight.auth.passwordreset.dto;

public record PasswordResetEmailRequest(
        String firstName,
        String email,
        String resetUrl
) {}
