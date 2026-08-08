package com.akademi.finsight.integration.infina.client.dto.fund;

import java.math.BigDecimal;

public record CumulativeReturnEntry(
        String fundCode,
        BigDecimal fundReturn,
        String fundDate,
        BigDecimal bmReturn,
        String bmDate
) {}
