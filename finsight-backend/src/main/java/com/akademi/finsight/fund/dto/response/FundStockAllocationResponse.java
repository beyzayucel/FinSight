package com.akademi.finsight.fund.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FundStockAllocationResponse(
        UUID id,
        UUID fundId,
        String period,
        String assetCode,
        BigDecimal weight,
        Instant createdAt
) {}
