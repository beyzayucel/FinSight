package com.akademi.finsight.integration.infina.dto.response.index;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexPriceResponse(
        String recordId,
        String recordDate,
        String assetName,
        String assetCode,
        BigDecimal closePrice,
        LocalDate dataDate,
        String currency
) {}
