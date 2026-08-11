package com.akademi.finsight.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MarketInput(
        @JsonProperty("usd_return_ratio") BigDecimal usdReturn,
        @JsonProperty("gold_return_ratio") BigDecimal goldReturn,
        @JsonProperty("brent_return_ratio") BigDecimal brentReturn,
        @JsonProperty("us10y_return_ratio") BigDecimal us10yReturn,
        @JsonProperty("cds_spread_bps") BigDecimal cdsSpreadBps,
        @JsonProperty("annual_inflation_percent") BigDecimal annualInflation,
        @JsonProperty("policy_rate_percent") BigDecimal policyRate
) {
}
