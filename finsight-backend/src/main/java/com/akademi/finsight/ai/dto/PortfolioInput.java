package com.akademi.finsight.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PortfolioInput(
        @JsonProperty("weights") PortfolioWeights weights,
        @JsonIgnore StockBreakdownRatios stockBreakdownRatios
) {
    public static PortfolioInput ofWeightsOnly(PortfolioWeights weights) {
        return new PortfolioInput(weights, null);
    }
}
