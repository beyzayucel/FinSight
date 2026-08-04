package com.akademi.finsight.integration.infina.client.dto.fund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FundAssetDistribution(
		String typeCode,
		BigDecimal ratio,
		String description,
		String descriptionEn,
		String shortDesc,
		String shortDescEn
){}