package com.akademi.finsight.fund.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FundChatRequest(

        @Schema(description = "Conversation to continue. Omit to start a new one; the id is returned in the response.",
                example = "6f1c2b7e-4b0e-4a54-9f6d-2f9e8f1a0c33")
        @Pattern(regexp = "^[A-Za-z0-9-]{1,64}$", message = "{validation.fund.chat.session}")
        String sessionId,

        @Schema(description = "The user's question", example = "Bu fonun son 30 günlük getirisi ne?")
        @NotBlank
        @Size(max = 1000)
        String message,

        @Schema(description = "Answer language. Overrides the Accept-Language header when present.",
                allowableValues = {"tr", "en"}, example = "en")
        @Pattern(regexp = "^(?i)(tr|en)$", message = "{validation.fund.chat.language}")
        String language
) {}
