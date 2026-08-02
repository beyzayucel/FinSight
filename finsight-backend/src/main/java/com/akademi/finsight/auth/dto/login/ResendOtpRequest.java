package com.akademi.finsight.auth.dto.login;

import com.akademi.finsight.auth.validation.IdentifierFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to resend OTP code")
public record ResendOtpRequest(

        @Schema(description = "Email or username", example = "admin")
        @NotBlank(message = "{validation.identifier.required}")
        @IdentifierFormat
        String identifier
) {}
