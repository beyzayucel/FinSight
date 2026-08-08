package com.akademi.finsight.integration.infina.client.dto.economic;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EconomicPriceRow(
        String recordId,
        String period,
        String recordDate,
        String assetName,
        String assetCode,
        BigDecimal price,
        LocalDate dataDate
) {}
