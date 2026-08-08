package com.akademi.finsight.integration.infina.dto.response.economic;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EconomicPriceResponse(
        String recordId,
        String period,
        String recordDate,
        String assetName,
        String assetCode,
        BigDecimal price,
        LocalDate dataDate
) {}
