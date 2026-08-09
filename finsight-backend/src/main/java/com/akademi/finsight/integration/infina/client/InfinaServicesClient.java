package com.akademi.finsight.integration.infina.client;

import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfoData;
import com.akademi.finsight.integration.infina.client.dto.base.InfinaResponse;
import com.akademi.finsight.integration.infina.client.dto.fund.*;
import com.akademi.finsight.integration.infina.client.dto.fx.FxPriceData;
import com.akademi.finsight.integration.infina.client.dto.index.IndexPriceData;
import com.akademi.finsight.integration.infina.client.dto.economic.EconomicPriceData;
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

	@GetExchange(InfinaEndpoints.FUND_DAILY_RETURN)
	InfinaResponse<FundDailyReturnData> getFundDailyReturn(
			@RequestParam(value = "fund_code") String fundCode,
			@RequestParam(value = "dates") String dates
	);


	@GetExchange(InfinaEndpoints.FX_PRICE)
	InfinaResponse<FxPriceData> getFxPrices(
			@RequestParam(value = "asset_code", required = false) String assetCode,
			@RequestParam(value = "data_date", required = false) String dataDate
	);

	@GetExchange(InfinaEndpoints.INDEX_PRICE)
	InfinaResponse<IndexPriceData> getIndexPrices(
			@RequestParam(value = "asset_code", required = false) String assetCode,
			@RequestParam(value = "data_date", required = false) String dataDate
	);

	@GetExchange(InfinaEndpoints.ECONOMIC_PRICE)
	InfinaResponse<EconomicPriceData> getEconomicPrices(
			@RequestParam(value = "asset_code", required = false) String assetCode,
			@RequestParam(value = "data_date", required = false) String dataDate
	);

	@GetExchange(InfinaEndpoints.FUND_PRICE)
	InfinaResponse<FundPriceData> getFundPrices(
			@RequestParam(value = "fund_code", required = false) String fundCode,
			@RequestParam(value = "data_date", required = false) String dataDate
	);
}
