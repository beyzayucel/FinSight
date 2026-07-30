package com.akademi.finsight.auth.refreshtoken.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(description = "Refresh token from login response", example = "dGhpcyBpcyBhIHNhbXBsZSByZWZyZXNoIHRva2Vu")
        @NotBlank(message = "{validation.refresh.token.required}")
        String refreshToken
) {
}
