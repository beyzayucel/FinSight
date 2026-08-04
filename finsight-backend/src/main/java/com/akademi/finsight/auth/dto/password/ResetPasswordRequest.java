package com.akademi.finsight.auth.dto.password;

import com.akademi.finsight.auth.validation.PasswordStrength;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(

        @Schema(description = "Password reset token received via email")
        @NotBlank(message = "{validation.password.reset.token.required}")
        String token,

        @Schema(description = "New password (min 8 chars, uppercase, lowercase, digit, special)", example = "NewPass456!")
        @NotBlank(message = "{validation.password.required}")
        @PasswordStrength
        String newPassword
) {}
