package com.akademi.finsight.integration.infina.dto.response.fx;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FxPriceResponse(
        String recordId,
        String recordDate,
        String assetName,
        String assetCode,
        BigDecimal ask,
        BigDecimal bid,
        LocalDate dataDate,
        String marketCode
) {}
