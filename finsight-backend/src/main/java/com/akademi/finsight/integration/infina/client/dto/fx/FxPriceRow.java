package com.akademi.finsight.integration.infina.client.dto.fx;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FxPriceRow(
        String recordId,
        String recordDate,
        String assetName,
        String assetCode,
        BigDecimal ask,
        BigDecimal bid,
        LocalDate dataDate,
        String marketCode
) {}
