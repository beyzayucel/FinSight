package com.akademi.finsight.fund.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FundPeriodMetricRequest(

        @Schema(description = "Id of the fund this metric belongs to",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "{validation.fund.id.required}")
        UUID fundId,

        @Schema(description = "Date the metric was calculated for", example = "2026-08-05")
        @NotNull(message = "{validation.data.date.required}")
        LocalDate dataDate,

        @Schema(description = "Period code the metric was calculated for. Format: PXD / PXW / PXM / PXY / XYTD / XDyyyy-MM-dd",
                example = "P10D")
        @NotBlank(message = "{validation.period.required}")
        @Size(max = 16, message = "{validation.period.size}")
        String period,

        @Schema(description = "Total value of the fund (matches DECIMAL(19,4))", example = "125000000.0000")
        @NotNull(message = "{validation.total.value.required}")
        @DecimalMin(value = "0.0", message = "{validation.total.value.positive}")
        @Digits(integer = 15, fraction = 4, message = "{validation.total.value.format}")
        BigDecimal totalValue,

        @Schema(description = "Total value of the fund at the start of the period (matches DECIMAL(19,4))",
                example = "120000000.0000")
        @DecimalMin(value = "0.0", message = "{validation.total.value.positive}")
        @Digits(integer = 15, fraction = 4, message = "{validation.total.value.format}")
        BigDecimal previousTotalValue,

        @Schema(description = "Date the period is measured from", example = "2026-07-06")
        LocalDate previousDate,

        @Schema(description = "Return of the fund on its last trading day (matches DECIMAL(9,6))", example = "1.888310")
        @NotNull(message = "{validation.daily.return.required}")
        @Digits(integer = 3, fraction = 6, message = "{validation.daily.return.format}")
        BigDecimal dailyReturn,

        @Schema(description = "Cumulative return over the period (matches DECIMAL(12,6))", example = "0.154300")
        @NotNull(message = "{validation.cumulative.return.required}")
        @Digits(integer = 6, fraction = 6, message = "{validation.cumulative.return.format}")
        BigDecimal cumulativeReturn,

        @Schema(description = "Benchmark return over the period (matches DECIMAL(12,6)); empty when the fund has no benchmark",
                example = "-1.937454")
        @Digits(integer = 6, fraction = 6, message = "{validation.benchmark.return.format}")
        BigDecimal benchmarkReturn,

        @Schema(description = "When the metric was fetched from its source; defaults to now on create and stays unchanged on update when omitted",
                example = "2026-08-05T10:15:30Z")
        Instant fetchedAt
) {}
