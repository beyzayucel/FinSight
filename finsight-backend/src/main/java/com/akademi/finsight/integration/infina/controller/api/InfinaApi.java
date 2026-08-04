package com.akademi.finsight.integration.infina.controller.api;

import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.integration.infina.dto.response.BenchmarkInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/infina")
@Tag(name = "Infina Integration", description = "Infina fund data operations.")
public interface InfinaApi {

	@Operation(summary = "Get benchmark info", description = "Returns benchmark yield and fund yield.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Benchmark info retrieved successfully."),
			@ApiResponse(responseCode = "502", description = "Infina returned an error response."),
			@ApiResponse(responseCode = "503", description = "Infina service is currently unavailable.",
					content = @Content(schema = @Schema(implementation = ApiStandardResponse.class)))
	})

	@GetMapping("/BenchmarkInfo")
	ResponseEntity<ApiStandardResponse<List<BenchmarkInfoResponse>>> benchmarkInfo(
			@Parameter(description = "Fund Code", example = "TIE")
			String fundCode,

			@Parameter(description = "Start date (yyyy-MM-dd)", example = "2024-01-01")
			String beginPeriod,

			@Parameter(description = "End date (yyyy-MM-dd)", example = "2024-02-01")
			String endPeriod,

			@Parameter(description = "Currency", example = "TRY")
			String currency
	);
}
