package com.akademi.finsight.integration.infina.dto.response;

import java.math.BigDecimal;

public record BenchmarkInfoResponse (
		BigDecimal benchmarkYield,
		BigDecimal fundYield
){}
