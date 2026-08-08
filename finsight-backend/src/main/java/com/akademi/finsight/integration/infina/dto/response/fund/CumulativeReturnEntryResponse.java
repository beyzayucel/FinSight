package com.akademi.finsight.integration.infina.dto.response.fund;

import java.math.BigDecimal;

public record CumulativeReturnEntryResponse(
        String fundCode,
        BigDecimal fundReturn,
        String fundDate,
        BigDecimal bmReturn
) {}
