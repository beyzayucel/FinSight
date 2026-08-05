package com.akademi.finsight.integration.infina.client;

import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfoData;
import com.akademi.finsight.integration.infina.client.dto.base.InfinaResponse;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.client.dto.fund.FundPortfolioAllocationData;
import com.akademi.finsight.integration.infina.constant.InfinaEndpoints;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InfinaServicesClient {

	@GetExchange(InfinaEndpoints.BENCHMARK_INFO)
	InfinaResponse<BenchmarkInfoData> getBenchmarkInfo (
			@RequestParam(value = "fund_code") String fundCode,
			@RequestParam(value = "begin_period") String beginPeriod,
			@RequestParam(value = "end_period") String endPeriod,
			@RequestParam(value = "currency", required = false) String currency);

	@GetExchange(InfinaEndpoints.FUND_INFO)
	InfinaResponse<FundInfoData> getFundInfo(
			@RequestParam(value = "fund_code") String fundCode,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "periods", required = false) String periods
	);

	@GetExchange(InfinaEndpoints.FUND_PORTFOLIO_ALLOCATION)
	InfinaResponse<FundPortfolioAllocationData> getFundPortfolioAllocation(
			@RequestParam(value = "fund_code") String fundCode,
			@RequestParam(value = "period", required = false) String period,
			@RequestParam(value = "disclosure_id", required = false) Long disclosureId
	);
}
