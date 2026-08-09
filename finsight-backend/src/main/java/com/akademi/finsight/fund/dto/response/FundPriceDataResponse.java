package com.akademi.finsight.fund.dto.response;

import com.akademi.finsight.fund.entity.FundPriceData;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FundPriceDataResponse(
        String fundCode,
        LocalDate dataDate,
        BigDecimal price,
        BigDecimal activeValue,
        BigDecimal portfolioValue,
        BigDecimal cashValue,
        BigDecimal investorCount,
        Instant fetchedAt
) {
    public static FundPriceDataResponse from(FundPriceData entity) {
        if (entity == null) {
            return null;
        }
        return new FundPriceDataResponse(
                entity.getFund() != null ? entity.getFund().getCode() : null,
                entity.getDataDate(),
                entity.getPrice(),
                entity.getActiveValue(),
                entity.getPortfolioValue(),
                entity.getCashValue(),
                entity.getInvestorCount(),
                entity.getFetchedAt()
        );
    }
}
