package com.akademi.finsight.stresstest.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.enums.SimulationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RequestMapping(ApiEndpoints.StressTest.BASE)
@Tag(name = "Stress Test", description = "Fund stress test simulation operations and latest result querying.")
public interface StressTestApi {

    @Operation(
            summary = "Run stress test simulation",
            description = "Executes an ONNX-backed stress test simulation for a specific fund and user portfolio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stress test simulation completed successfully."),
            @ApiResponse(responseCode = "404", description = "User or Fund not found.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class))),
            @ApiResponse(responseCode = "500", description = "ONNX Model execution failed.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class)))
    })
    @PostMapping(ApiEndpoints.StressTest.RUN)
    ResponseEntity<ApiStandardResponse<StressTestInferenceResponseDto>> runSimulation(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Parameter(description = "Fund UUID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @RequestParam("fundId") @NotNull UUID fundId,
            @Parameter(description = "Simulation Scenario Type", example = "EQUITY_SHOCK", required = true)
            @RequestParam("simulationType") @NotNull SimulationType simulationType
    );

    @Operation(
            summary = "Get latest stress test result",
            description = "Retrieves the most recent stress test simulation result for the specified fund. Returns 204 No Content if no test has been executed yet."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Latest stress test result retrieved successfully."),
            @ApiResponse(responseCode = "204", description = "No stress test result found for this fund."),
            @ApiResponse(responseCode = "404", description = "User or Fund not found.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class)))
    })
    @GetMapping(ApiEndpoints.StressTest.LATEST)
    ResponseEntity<ApiStandardResponse<StressTestInferenceResponseDto>> getLatestSimulationResult(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Fund UUID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @RequestParam("fundId") @NotNull UUID fundId
    );
}
