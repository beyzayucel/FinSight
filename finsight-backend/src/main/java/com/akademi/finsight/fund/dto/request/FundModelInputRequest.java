package com.akademi.finsight.fund.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FundModelInputRequest(
        BigDecimal stockWeight,
        BigDecimal repoWeight,
        BigDecimal futureWeight,
        BigDecimal fundWeight,

        BigDecimal usdReturn,
        BigDecimal goldReturn,
        BigDecimal brentReturn,
        BigDecimal us10yReturn,
        BigDecimal cdsSpreadBps,
        BigDecimal annualInflation,
        BigDecimal policyRate,

        BigDecimal fundReturn,
        BigDecimal portfolioGrowth,

        BigDecimal activeValue,
        BigDecimal portfolioValue,
        BigDecimal cashValue,
        BigDecimal investorCount
) {}
