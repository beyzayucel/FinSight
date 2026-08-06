package com.akademi.finsight.fund.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record FundStockAllocationRequest(

        @Schema(description = "Id of the fund this allocation belongs to",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "{validation.fund.id.required}")
        UUID fundId,

        @Schema(description = "Disclosure period (yyyy-MM)", example = "2026-05")
        @NotBlank(message = "{validation.allocation.period.required}")
        @Size(max = 7, message = "{validation.allocation.period.size}")
        String period,

        @Schema(description = "Code of the held asset", example = "THYAO")
        @NotBlank(message = "{validation.asset.code.required}")
        @Size(max = 32, message = "{validation.asset.code.size}")
        String assetCode,

        @Schema(description = "Weight of the asset within the fund (matches DECIMAL(9,6))", example = "0.062500")
        @NotNull(message = "{validation.weight.required}")
        @DecimalMin(value = "0.0", message = "{validation.weight.positive}")
        @Digits(integer = 3, fraction = 6, message = "{validation.weight.format}")
        BigDecimal weight
) {}
