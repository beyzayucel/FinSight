package com.akademi.finsight.integration.infina.client.dto.index;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record IndexPriceRow(
        String recordId,
        String recordDate,
        String assetName,
        String assetCode,
        BigDecimal closePrice,
        LocalDate dataDate,
        String currency
) {}
