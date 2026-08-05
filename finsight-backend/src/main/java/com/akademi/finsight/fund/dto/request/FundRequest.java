package com.akademi.finsight.fund.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FundRequest(

        @Schema(description = "Fund code (max 3 characters)", example = "AFA")
        @NotBlank(message = "{validation.fund.code.required}")
        @Size(max = 3, message = "{validation.fund.code.size}")
        String code,

        @Schema(description = "Fund date", example = "2026-08-05")
        @NotNull(message = "{validation.fund.date.required}")
        LocalDate date
) {}
