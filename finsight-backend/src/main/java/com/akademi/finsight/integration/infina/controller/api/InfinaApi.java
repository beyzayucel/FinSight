package com.akademi.finsight.integration.infina.controller.api;

import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.integration.infina.dto.response.BenchmarkInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/infina")
@Tag(name = "Infina Integration", description = "Infina fund data operations")
public interface InfinaApi {

	@Operation(summary = "Get benchmark info", description = "Returns benchmark yield and fund yield")
	@ApiResponse(responseCode = "200", description = "Benchmark info retrieved successfully")
	@GetMapping("/BenchmarkInfo")
	ResponseEntity<ApiStandardResponse<List<BenchmarkInfoResponse>>> benchmarkInfo(
			String fundCode,
			String beginPeriod,
			String endPeriod,
			String currency
	);
}
