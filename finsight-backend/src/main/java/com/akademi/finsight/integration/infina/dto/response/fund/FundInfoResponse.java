package com.akademi.finsight.integration.infina.dto.response.fund;

import java.math.BigDecimal;
import java.util.List;

public record FundInfoResponse (
		List<FundReturnResponse> periodReturns,
		BigDecimal totalMarketPrice,
		List<FundAssetDistributionResponse> assetDistribution
){}
