package com.akademi.finsight.auth.dto.password;

import com.akademi.finsight.auth.validation.PasswordStrength;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(

        @Schema(description = "Current password", example = "ChangeMe!2026")
        @NotBlank(message = "{validation.password.current.required}")
        String currentPassword,

        @Schema(description = "New password (min 8 chars, uppercase, lowercase, digit, special)", example = "NewPass456!")
        @NotBlank(message = "{validation.password.required}")
        @PasswordStrength
        String newPassword
) {}
