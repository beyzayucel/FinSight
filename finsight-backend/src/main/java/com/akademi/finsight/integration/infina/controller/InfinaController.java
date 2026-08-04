package com.akademi.finsight.integration.infina.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.integration.infina.controller.api.InfinaApi;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InfinaController extends BaseController implements InfinaApi {

	private final InfinaService infinaService;

	@Override
	public ResponseEntity<ApiStandardResponse<List<BenchmarkInfoResponse>>> benchmarkInfo(
			@RequestParam("fundCode") String fundCode,
			@RequestParam("beginPeriod") String beginPeriod,
			@RequestParam("endPeriod") String endPeriod,
			@RequestParam(value = "currency", required = false) String currency){
		return ok(infinaService.getBenchmarkInfo(fundCode, beginPeriod, endPeriod, currency));
	}

	@Override
	public ResponseEntity<ApiStandardResponse<FundInfoResponse>> fundInfo(
			@RequestParam("fundCode") String fundCode,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "period", required = false) String period){
		return ok(infinaService.getFundInfo(fundCode, date, period));
	}
}
