package com.akademi.finsight.fund.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FundDistributionRequest(

        @Schema(description = "Id of the fund this distribution belongs to",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "{validation.fund.id.required}")
        UUID fundId,

        @Schema(description = "Distribution category", example = "Equity")
        @NotBlank(message = "{validation.category.required}")
        String category,

        @Schema(description = "Weight of the category (matches DECIMAL(9,6))", example = "0.253400")
        @NotNull(message = "{validation.weight.required}")
        @DecimalMin(value = "0.0", message = "{validation.weight.positive}")
        @Digits(integer = 3, fraction = 6, message = "{validation.weight.format}")
        BigDecimal weight,

        @Schema(description = "Distribution date", example = "2026-08-05")
        @NotNull(message = "{validation.date.required}")
        LocalDate date
) {}
