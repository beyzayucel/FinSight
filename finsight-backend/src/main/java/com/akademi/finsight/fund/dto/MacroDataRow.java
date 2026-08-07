package com.akademi.finsight.fund.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MacroDataRow(
        LocalDate date,
        BigDecimal usdReturn,
        BigDecimal goldReturn,
        BigDecimal brentReturn,
        BigDecimal us10yReturn,
        BigDecimal cdsSpreadBps,
        BigDecimal annualInflation,
        BigDecimal policyRate
) {}
