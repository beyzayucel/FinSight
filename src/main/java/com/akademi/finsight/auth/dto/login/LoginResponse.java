package com.akademi.finsight.auth.dto.login;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        boolean firstLogin
) {
    public LoginResponse(String accessToken, String refreshToken, long expiresIn, boolean firstLogin) {
        this(accessToken, refreshToken, "Bearer", expiresIn, firstLogin);
    }
}
