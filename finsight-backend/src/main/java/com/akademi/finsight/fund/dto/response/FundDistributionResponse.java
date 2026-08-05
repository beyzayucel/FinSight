package com.akademi.finsight.fund.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FundDistributionResponse(
        UUID id,
        UUID fundId,
        String category,
        BigDecimal weight,
        LocalDate date,
        Instant createdAt
) {}
