package com.akademi.finsight.ai.dto.request;

import com.akademi.finsight.ai.dto.FundInput;
import com.akademi.finsight.ai.dto.MarketInput;
import com.akademi.finsight.ai.dto.PortfolioInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/// /** POST /api/v1/decisions için istek gövdesi. Ekstra alan kabul edilmez. */
public record DecisionRequest(
        @NotNull @Valid MarketInput market,
        @NotNull @Valid FundInput fund,
        @NotNull @Valid PortfolioInput portfolio
) {
}
