package com.akademi.finsight.stresstest.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.request.SaveStressTestDecisionRequestDto;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.enums.SimulationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping(ApiEndpoints.StressTest.BASE)
@Tag(name = "Stress Test", description = "Fund stress test simulation operations and latest result querying.")
public interface StressTestApi {

    @Operation(
            summary = "Run stress test simulation",
            description = "Executes a stress test simulation using the portfolio composition sent by the client."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stress test simulation completed successfully."),
            @ApiResponse(responseCode = "404", description = "User or Fund not found.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class))),
            @ApiResponse(responseCode = "500", description = "Model execution failed.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class)))
    })
    @PostMapping(ApiEndpoints.StressTest.RUN)
    ResponseEntity<ApiStandardResponse<StressTestInferenceResponseDto>> runSimulation(
            @AuthenticationPrincipal String userEmail,
            @RequestParam("fundId") String fundId,
            @RequestParam("simulationType") SimulationType simulationType,
            @Valid @RequestBody PortfolioDataDto portfolioDataDto);

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
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Parameter(description = "Fund UUID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @RequestParam("fundId") @NotNull String fundId
    );

    @Operation(
            summary = "Get stress test result by lookback period",
            description = "Retrieves the historical stress test result matching the selected analysis period (e.g., 10, 20, 30, 90 days ago). Returns 204 No Content if no record matches the given period."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stress test result for the specified period retrieved successfully."),
            @ApiResponse(responseCode = "204", description = "No stress test result found for this lookback period."),
            @ApiResponse(responseCode = "404", description = "User or Fund not found.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class)))
    })
    @GetMapping(ApiEndpoints.StressTest.PERIOD)
    ResponseEntity<ApiStandardResponse<StressTestInferenceResponseDto>> getSimulationResultByPeriod(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Parameter(description = "Fund UUID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @RequestParam("fundId") @NotNull String fundId,
            @Parameter(description = "Analysis period in days ago", example = "30", required = true)
            @RequestParam(value = "daysAgo", defaultValue = "30") String daysAgo
    );

    @Operation(
            summary = "Attach a stress test result to the latest decision",
            description = "\"Karar Geçmişine Kaydet\" — links an already-persisted stress test result (the id " +
                          "returned by /run) to whichever decision (AI or Manual) is currently the most recent " +
                          "for this fund. Does not re-run the simulation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stress test result attached to the latest decision."),
            @ApiResponse(responseCode = "403", description = "Stress test result does not belong to this user/fund.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stress test result not found, or no decision exists to attach it to.",
                    content = @Content(schema = @Schema(implementation = ApiStandardResponse.class)))
    })
    @PostMapping(ApiEndpoints.StressTest.SAVE)
    ResponseEntity<ApiStandardResponse<Void>> saveDecisionRecord(
            @AuthenticationPrincipal String userEmail,
            @Valid @RequestBody SaveStressTestDecisionRequestDto requestDto
    );
}