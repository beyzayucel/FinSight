package com.akademi.finsight.integration.infina.client.dto.benchmark;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BenchmarkInfo(
		LocalDate bmDefinitionDate,
		String bmDefinition,
		LocalDate bmBeginPeriod,
		LocalDate bmEndPeriod,
		BigDecimal bmYield,
		LocalDate fundBeginPeriod,
		LocalDate fundEndPeriod,
		BigDecimal fundYield
) {}