package com.akademi.finsight.integration.infina.dto.response.fund;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FundInfoResponse (
		String name,
		LocalDate date,
		LocalDate fundDate,
		List<FundReturnResponse> periodReturns,
		BigDecimal totalMarketPrice,
		List<FundAssetDistributionResponse> assetDistribution
){}
