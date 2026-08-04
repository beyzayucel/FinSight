package com.akademi.finsight.auth.dto.password;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(

        @Schema(description = "Account email address", example = "john.doe@infina.com")
        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.format}")
        @Size(max = 320, message = "{validation.email.size}")
        String email
) {}
