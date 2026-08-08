package com.akademi.finsight.integration.infina.service;

import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.CumulativeReturnEntryResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundDailyReturnResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
import com.akademi.finsight.integration.infina.dto.response.fx.FxPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.index.IndexPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.economic.EconomicPriceResponse;

import java.util.List;

public interface InfinaService {

	List<FxPriceResponse> getFxPrices(String assetCode, String dataDate);

	List<IndexPriceResponse> getIndexPrices(String assetCode, String dataDate);

	List<EconomicPriceResponse> getEconomicPrices(String assetCode, String dataDate);

	List<BenchmarkInfoResponse> getBenchmarkInfo(String fundCode,
												 String beginPeriod,
												 String endPeriod,
												 String currency);

	FundInfoResponse getFundInfo(String fundCode,
								 String date,
								 String periods);

	List<FundPortfolioAllocationResponse> getFundPortfolioAllocation(String fundCode,
																	 String period,
																	 Long disclosureId);

	FundDailyReturnResponse getFundDailyReturn(String fundCode, String dates);


}
