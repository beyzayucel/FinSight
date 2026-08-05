package com.akademi.finsight.integration.infina.client;

import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfoData;
import com.akademi.finsight.integration.infina.client.dto.base.InfinaResponse;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.constant.InfinaEndpoints;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InfinaServicesClient {

	@GetExchange(InfinaEndpoints.BENCHMARK_INFO)
	InfinaResponse<BenchmarkInfoData> getBenchmarkInfo (
			@RequestParam(value = "fund_code", required = true) String fundCode,
			@RequestParam(value = "begin_period", required = true) String beginPeriod,
			@RequestParam(value = "end_period", required = true) String endPeriod,
			@RequestParam(value = "currency", required = false) String currency);

	@GetExchange(InfinaEndpoints.FUND_INFO)
	InfinaResponse<FundInfoData> getFundInfo(
			@RequestParam(value = "fund_code", required = true) String fundCode,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "periods", required = false) String periods
	);
}
