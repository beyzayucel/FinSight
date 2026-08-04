package com.akademi.finsight.integration.infina.client.dto.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FundBenchmarkIndex(
		String ratio,
		BigDecimal rate,
		String description,
		@JsonProperty("return") BigDecimal returnRate
){}