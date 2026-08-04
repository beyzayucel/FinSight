package com.akademi.finsight.integration.infina.client.dto.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FundInfo(
		String description,
		String fundManager,
		String fundFounder,
		String mkkCode,
		String isinCode,
		@JsonProperty("class") String fundClass,
		String classEn,
		boolean qualifiedInvestor,
		String group,
		String type,
		LocalDate issueDate,
		String currency,
		String tefas,
		String isActive,
		List<String> tags,
		BigDecimal totalMv,
		BigDecimal investorCount,
		Long shares,
		BigDecimal totalShares,
		BigDecimal sharesRatio,
		BigDecimal price,
		BigDecimal firstPrice,
		String riskValue,
		BigDecimal fundFeeDaily,
		BigDecimal fundFeeYearly,
		List<String> portfolioManagers,
		String investmentPurpose,
		String kapUrl,
		List<FundBenchmarkIndex> bmIndex
){}