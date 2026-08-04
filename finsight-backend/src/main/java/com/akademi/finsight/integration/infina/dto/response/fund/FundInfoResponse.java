package com.akademi.finsight.integration.infina.dto.response.fund;

import java.math.BigDecimal;
import java.util.List;

public record FundInfoResponse (
		BigDecimal fundDailyYield,
		BigDecimal totalMarketPrice,
		List<FundAssetDistributionResponse> assetDistribution
){}
