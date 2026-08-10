package com.akademi.finsight.fund.dto.response;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


public record FundPeriodMetricResponse(
        UUID id,
        UUID fundId,
        LocalDate dataDate,
        String period,
        BigDecimal totalValue,
        BigDecimal previousTotalValue,
        LocalDate previousDate,
        BigDecimal dailyReturn,
        BigDecimal cumulativeReturn,
        BigDecimal benchmarkReturn,
        BigDecimal benchmarkDiffBps,
        Instant fetchedAt,
        Instant createdAt
) {

}
