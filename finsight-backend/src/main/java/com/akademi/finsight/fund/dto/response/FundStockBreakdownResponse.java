package com.akademi.finsight.fund.dto.response;

import java.util.List;

public record FundStockBreakdownResponse(
        String period,
        List<FundStockWeightResponse> items
) {}
