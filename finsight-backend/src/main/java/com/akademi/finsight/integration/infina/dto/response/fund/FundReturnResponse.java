package com.akademi.finsight.integration.infina.dto.response.fund;

import java.math.BigDecimal;

public record FundReturnResponse(
		String period,
		BigDecimal yield
){}
