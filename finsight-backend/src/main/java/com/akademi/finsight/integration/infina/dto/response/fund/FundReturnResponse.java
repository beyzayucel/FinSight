package com.akademi.finsight.integration.infina.dto.response.fund;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FundReturnResponse(
		String period,
		BigDecimal fundReturn,
		LocalDate beginDate,
		BigDecimal benchmarkReturn
){}
