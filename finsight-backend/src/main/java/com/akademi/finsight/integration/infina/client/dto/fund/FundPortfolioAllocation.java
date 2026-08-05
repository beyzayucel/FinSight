package com.akademi.finsight.integration.infina.client.dto.fund;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FundPortfolioAllocation(
		String assetCode,
		String mkDetail,
		String fundCode,
		String period,
		String pdfType,
		BigDecimal nominalValue,
		BigDecimal totalValue,
		BigDecimal groupPercentage,
		BigDecimal totalTdPercentage,
		BigDecimal totalPdPercentage,
		OffsetDateTime createdDate,
		OffsetDateTime updatedDate,
		OffsetDateTime disclosureTime,
		LocalDate disclosureDate,
		Long disclosureId,
		String mkType,
		String mkSubtype
){}
