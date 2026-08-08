package com.akademi.finsight.fund.performancecomparison.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(ApiEndpoints.PerformanceComparison.BASE)
@Tag(
        name = "Performance Comparison",
        description = "Cumulative return curves and performance metrics for current, simulation and benchmark portfolios"
)
public interface PerformanceComparisonApi {

    @Operation(summary = "Compare portfolio performance",
            description = """
                    Returns cumulative return curves and performance summary metrics
                    (current value, total return, max drawdown, daily volatility)
                    for the given fund and analysis window (10, 20, 30 or 90 days).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance comparison retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Fund not found or no metric for requested period")
    })
    @GetMapping(ApiEndpoints.PerformanceComparison.COMPARE)
    ResponseEntity<ApiStandardResponse<PerformanceComparisonResponse>> compare(
            @Parameter(description = "Fund code", example = "TIE")
            @PathVariable String fundCode,
            @Parameter(description = "Analysis window in days", example = "30")
            @RequestParam(defaultValue = "30") int analysisWindow);
}
