package com.akademi.finsight.integration.infina.client.dto.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FundInfoData(
		String fundCode,
		LocalDate date,
		LocalDate fundDate,
		LocalDate benchmarkDate,
		List<String> periods,
		List<LocalDate> fundBeginDates,
		List<LocalDate> bmBeginDates,
		List<BigDecimal> investorChangeRate,
		List<BigDecimal> avgShares,
		FundInfo fundDetail,
		List<BigDecimal> fundReturn,
		List<BigDecimal> fundReturnAnn,
		FundDepositComparison depositCon,
		List<BigDecimal> fundBenchmark,
		List<BigDecimal> standardDev,
		List<Integer> positiveDays,
		List<Integer> negativeDays,
		List<Integer> totalDays,
		List<BigDecimal> positiveDayRates,
		List<FundAssetDistribution> fundDistribution
){}
