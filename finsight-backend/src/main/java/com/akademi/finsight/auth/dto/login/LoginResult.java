package com.akademi.finsight.auth.dto.login;

public sealed interface LoginResult {

    record Authenticated(
            String type,
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            boolean firstLogin
    ) implements LoginResult {
        public Authenticated(String accessToken, String refreshToken, long expiresIn, boolean firstLogin) {
            this("AUTHENTICATED", accessToken, refreshToken, "Bearer", expiresIn, firstLogin);
        }
    }

    record OtpRequired(
            String type,
            String message
    ) implements LoginResult {
        public OtpRequired(String message) {
            this("OTP_REQUIRED", message);
        }
    }
}
