package com.akademi.finsight.fund.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record FundSyncResponse(
        UUID fundId,
        String fundCode,
        LocalDate dataDate,
        int periodMetricCount,
        int distributionCount,
        String allocationPeriod,
        int stockAllocationCount
) {}
