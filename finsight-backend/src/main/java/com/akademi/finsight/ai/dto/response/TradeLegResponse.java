package com.akademi.finsight.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TradeLegResponse(
        @JsonProperty("source") String source,
        @JsonProperty("target") String target,
        @JsonProperty("amount_ratio") BigDecimal amountRatio
) {
}
