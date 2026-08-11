package com.akademi.finsight.integration.infina.dto.response.fund;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FundPriceResponse(
        String fundCode,
        LocalDate dataDate,
        BigDecimal price,
        BigDecimal activeValue,
        BigDecimal portfolioValue,
        BigDecimal cashValue,
        BigDecimal investorCount
) {}
