package com.akademi.finsight.ai.dto.request;

import com.akademi.finsight.ai.dto.FundInput;
import com.akademi.finsight.ai.dto.MarketInput;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PortfolioDecisionApiRequest(
        @Valid @NotNull @JsonProperty("market") MarketInput market,
        @Valid @NotNull @JsonProperty("fund") FundInput fund
) {
}
