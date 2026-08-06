package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(ApiEndpoints.Funds.BASE)
@Tag(
        name = "Fund Dashboard",
        description = "Single payload backing the fund dashboard screen"
)
public interface FundDashboardApi {

    @Operation(summary = "Get fund dashboard",
            description = """
                    Returns the fund dashboard for the most recent synced data date: every analysis period,
                    the asset distribution and the stock breakdown. Switching the analysis period and
                    flipping the total value card are both client-side, as all of it is in this payload.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fund dashboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Fund not found or not synced yet")
    })
    @GetMapping(ApiEndpoints.Funds.DASHBOARD)
    ResponseEntity<ApiStandardResponse<FundDashboardResponse>> getDashboard(
            @Parameter(description = "Fund code", example = "TIE")
            @PathVariable String fundCode);
}
