package com.akademi.finsight.auth.dto.login;

import com.akademi.finsight.auth.validation.IdentifierFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(description = "Email or username", example = "admin")
        @NotBlank(message = "{validation.identifier.required}")
        @IdentifierFormat
        String identifier,

        @Schema(description = "Password", example = "ChangeMe!2026")
        @NotBlank(message = "{validation.password.required}")
        String password
) {}
