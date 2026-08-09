package com.akademi.finsight.fund.dto.response;

import com.akademi.finsight.fund.entity.MarketData;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketDataResponse(
        LocalDate date,
        BigDecimal usdReturn,
        BigDecimal goldReturn,
        BigDecimal brentReturn,
        BigDecimal us10yReturn,
        BigDecimal cdsSpreadBps,
        BigDecimal annualInflation,
        BigDecimal policyRate
) {
    public static MarketDataResponse from(MarketData entity) {
        if (entity == null) {
            return null;
        }
        return new MarketDataResponse(
                entity.getDate(),
                entity.getUsdReturn(),
                entity.getGoldReturn(),
                entity.getBrentReturn(),
                entity.getUs10yReturn(),
                entity.getCdsSpreadBps(),
                entity.getAnnualInflation(),
                entity.getPolicyRate()
        );
    }
}
