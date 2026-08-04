package com.akademi.finsight.integration.infina.service;

import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;

import java.util.List;

public interface InfinaService {
	List<BenchmarkInfoResponse> getBenchmarkInfo(String fundCode,
												 String beginPeriod,
												 String endPeriod,
												 String currency);

	FundInfoResponse getFundInfo(String fundCode,
								 String date,
								 String periods);
}
