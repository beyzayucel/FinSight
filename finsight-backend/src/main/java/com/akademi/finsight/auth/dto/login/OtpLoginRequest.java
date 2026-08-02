package com.akademi.finsight.auth.dto.login;

import com.akademi.finsight.auth.validation.IdentifierFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "OTP login request after 2FA code is received")
public record OtpLoginRequest(

        @Schema(description = "Email or username", example = "admin")
        @NotBlank(message = "{validation.identifier.required}")
        @IdentifierFormat
        String identifier,

        @Schema(description = "6-digit OTP code", example = "482916")
        @NotBlank(message = "{validation.otp.code.required}")
        String code
) {}
