package com.akademi.finsight.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FundInput(
        @JsonProperty("fund_return_ratio") BigDecimal fundReturn,
        @JsonProperty("portfolio_growth_ratio") BigDecimal portfolioGrowth,
        @JsonProperty("active_value_try") BigDecimal activeValue,
        @JsonProperty("cash_value_try") BigDecimal cashValue,
        @JsonProperty("investor_count") Integer investorCount
) {
}
