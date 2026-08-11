package com.akademi.finsight.fund.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FundRequest(

        @Schema(description = "Fund code (max 3 characters)", example = "TIE")
        @NotBlank(message = "{validation.fund.code.required}")
        @Size(max = 3, message = "{validation.fund.code.size}")
        String code,

        @Schema(description = "Fund name; filled in automatically by the Infina sync",
                example = "İŞ PORTFÖY BIST 30 ENDEKSİ HİSSE SENEDİ (TL) FONU")
        @Size(max = 255, message = "{validation.fund.name.size}")
        String name
) {}
