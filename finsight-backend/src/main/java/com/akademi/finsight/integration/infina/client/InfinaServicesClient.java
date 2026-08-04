package com.akademi.finsight.integration.infina.client;

import com.akademi.finsight.integration.infina.client.dto.BenchmarkInfoData;
import com.akademi.finsight.integration.infina.client.dto.base.InfinaResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InfinaServicesClient {

	@GetExchange("/BenchmarkInfo")
	InfinaResponse<BenchmarkInfoData> getBenchmarkInfo (
			@RequestParam(value = "fund_code", required = true) String fundCode,
			@RequestParam(value = "begin_period", required = true) String beginPeriod,
			@RequestParam(value = "end_period", required = true) String endPeriod,
			@RequestParam(value = "currency", required = false) String currency);
}
