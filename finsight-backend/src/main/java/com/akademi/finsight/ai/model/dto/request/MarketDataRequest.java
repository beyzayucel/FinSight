package com.akademi.finsight.ai.model.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketDataRequest(
        LocalDate date,
        BigDecimal usdReturn,
        BigDecimal goldReturn,
        BigDecimal brentReturn,
        BigDecimal us10yReturn,
        BigDecimal cdsSpreadBps,
        BigDecimal annualInflation,
        BigDecimal policyRate
) {}
