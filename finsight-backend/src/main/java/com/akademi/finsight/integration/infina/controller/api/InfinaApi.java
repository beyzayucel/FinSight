package com.akademi.finsight.integration.infina.controller.api;

import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
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

            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-01-01")
            String beginPeriod,

            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-02-01")
            String endPeriod,

            @Parameter(description = "Currency", example = "TRY")
            String currency
    );

    @Operation(summary = "Get fund infos.", description = "Returns fund daily yield, total market price and asset distribution.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fund info retrieved successfully."),
            @ApiResponse(responseCode = "502", description = "Infina returned an error response."),
            @ApiResponse(responseCode = "503", description = "Infina service is currently unavailable.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class)))
    })

    @GetMapping("/FundInfo")
    ResponseEntity<ApiStandardResponse<FundInfoResponse>> fundInfo(
            @Parameter(description = "Fund Code", example = "TIE")
            String fundCode,

            @Parameter(description = "Date (yyyy-MM-dd)", example = "2026-02-01")
            String date,

            @Parameter(description = "Period codes (comma-separated). "
                    + "Format: PXD / PXW / PXM / PXY / XYTD / XDyyyy-MM-dd.<br>"
                    + "Default: P1D,P1M,P3M,P6M,XYTD,P1Y",
                    example = "P15D")
            String periods
    );

    @Operation(summary = "Get fund portfolio allocation",
            description = "Returns the asset breakdown of a fund's disclosed portfolio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fund portfolio allocation retrieved successfully."),
            @ApiResponse(responseCode = "502", description = "Infina returned an error response."),
            @ApiResponse(responseCode = "503", description = "Infina service is currently unavailable.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class)))
    })

    @GetMapping("/FundPortfolioAllocation")
    ResponseEntity<ApiStandardResponse<List<FundPortfolioAllocationResponse>>> fundPortfolioAllocation(
            @Parameter(description = "Fund Code", example = "TIE")
            String fundCode,

            @Parameter(description = "Disclosure period (yyyy-MM)", example = "2026-05")
            String period,

            @Parameter(description = "Disclosure id", example = "1359175")
            Long disclosureId
    );
}