package com.akademi.finsight.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record PortfolioWeights(
        @JsonProperty("stock_ratio") BigDecimal stockRatio,
        @JsonProperty("repo_ratio") BigDecimal repoRatio,
        @JsonProperty("collateral_ratio") BigDecimal collateralRatio,
        @JsonProperty("fund_ratio") BigDecimal fundRatio
) {
}
