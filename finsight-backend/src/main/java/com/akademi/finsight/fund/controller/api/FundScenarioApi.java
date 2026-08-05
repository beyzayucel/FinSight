package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.dto.request.ManualScenarioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiEndpoints.Funds.BASE)
@Tag(name = "Fund Manual Scenario", description = "Manual Simulation scenarios for funds")
public interface FundScenarioApi {
    @Operation(
            summary = "Apply manual scenario weights",
            description = "Validates and persists manual simulation weights entered in the Manual tab."
    )
    @ApiResponse(responseCode = "201", description = "Manual scenario applied and saved successfully")
    @ApiResponse(responseCode = "400", description = "Business rule validation error (Sum not 100%, Stock < 80%, or Deviation > 10%)")
    @ApiResponse(responseCode = "404", description = "Fund not found")
    @PostMapping(ApiEndpoints.Funds.MANUAL_SCENARIO)
    ResponseEntity<ApiStandardResponse<Void>> applyManualScenario(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ManualScenarioRequest request
    );
}