package com.akademi.finsight.ai.dto.response;

import com.akademi.finsight.ai.dto.PortfolioWeights;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record WeightsResponse(
        @JsonProperty("stock_ratio") BigDecimal stockRatio,
        @JsonProperty("repo_ratio") BigDecimal repoRatio,
        @JsonProperty("collateral_ratio") BigDecimal collateralRatio,
        @JsonProperty("fund_ratio") BigDecimal fundRatio
) {
    /** Bir sonraki decide() çağrısına doğrudan gönderilebilecek istek DTO'suna çevirir. */
    public PortfolioWeights toRequestWeights() {
        return new PortfolioWeights(stockRatio, repoRatio, collateralRatio, fundRatio);
    }
}
