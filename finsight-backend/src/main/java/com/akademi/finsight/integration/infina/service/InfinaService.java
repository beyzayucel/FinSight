package com.akademi.finsight.integration.infina.service;

import com.akademi.finsight.integration.infina.dto.response.BenchmarkInfoResponse;

import java.util.List;

public interface InfinaService {
	List<BenchmarkInfoResponse> getBenchmarkInfo(String fundCode,
												 String beginPeriod,
												 String endPeriod,
												 String currency);
}
