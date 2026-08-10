package com.akademi.finsight.integration.infina.dto.response.stock;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockPriceResponse(
        String assetCode,
        BigDecimal closePrice,
        LocalDate dataDate
) {}
