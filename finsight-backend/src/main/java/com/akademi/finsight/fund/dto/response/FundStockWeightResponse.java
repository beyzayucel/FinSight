package com.akademi.finsight.fund.dto.response;

import java.math.BigDecimal;

public record FundStockWeightResponse(
        String assetCode,
        BigDecimal weight
) {}
