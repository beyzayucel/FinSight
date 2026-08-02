package com.akademi.finsight.auth.dto.login;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public sealed interface LoginResult {

    @JsonTypeName("AUTHENTICATED")
    record Authenticated(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            boolean firstLogin
    ) implements LoginResult {
        public Authenticated(String accessToken, String refreshToken, long expiresIn, boolean firstLogin) {
            this(accessToken, refreshToken, "Bearer", expiresIn, firstLogin);
        }
    }

    @JsonTypeName("OTP_REQUIRED")
    record OtpRequired(
            String message
    ) implements LoginResult {}
}
