package com.akademi.finsight.integration.infina.client.dto.fund;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FundPortfolioAllocationData(
		@JsonProperty("FundPortfolioAllocation") List<FundPortfolioAllocation> allocations
){}
