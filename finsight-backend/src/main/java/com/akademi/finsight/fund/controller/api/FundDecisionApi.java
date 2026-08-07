package com.akademi.finsight.fund.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.dto.request.AttachMetricsRequest;
import com.akademi.finsight.fund.dto.request.AttachStressTestRequest;
import com.akademi.finsight.fund.dto.response.DecisionRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(ApiEndpoints.Funds.BASE)
@Tag(name = "Fund Decision History", description = "Unified (AI Recommendation + Manual Scenario) decision history")
public interface FundDecisionApi {

    @Operation(
            summary = "Get unified decision history",
            description = "Returns the authenticated user's AI Recommendation and Manual Scenario decisions for the given fund, merged and sorted newest first."
    )
    @ApiResponse(responseCode = "200", description = "Decision history retrieved successfully")
    @GetMapping(ApiEndpoints.Funds.DECISION_HISTORY)
    ResponseEntity<ApiStandardResponse<List<DecisionRecordResponse>>> getDecisionHistory(
            @AuthenticationPrincipal String email,
            @RequestParam UUID fundId
    );

    @Operation(
            summary = "Attach performance metrics to the latest decision",
            description = "Called right after Performance Comparison computes the simulation result — attaches " +
                          "it to whichever decision (AI Recommendation or Manual Scenario) is currently the most " +
                          "recent for this fund, without touching either decision's own apply/submit flow."
    )
    @ApiResponse(responseCode = "200", description = "Metrics attached successfully")
    @ApiResponse(responseCode = "404", description = "No decision exists to attach these metrics to")
    @PatchMapping(ApiEndpoints.Funds.DECISION_METRICS)
    ResponseEntity<ApiStandardResponse<Void>> attachMetrics(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody AttachMetricsRequest request
    );

    @Operation(
            summary = "Attach a stress test result to the latest decision",
            description = "Called from the Stress Test screen's \"Karar Geçmişine Kaydet\" action — links an " +
                          "already-computed stress test result to whichever decision (AI or Manual) is currently " +
                          "the most recent for this fund."
    )
    @ApiResponse(responseCode = "200", description = "Stress test result attached successfully")
    @ApiResponse(responseCode = "404", description = "Stress test result not found, or no decision exists to attach it to")
    @ApiResponse(responseCode = "403", description = "Stress test result does not belong to this user/fund")
    @PatchMapping(ApiEndpoints.Funds.DECISION_STRESS_TEST)
    ResponseEntity<ApiStandardResponse<Void>> attachStressTestResult(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody AttachStressTestRequest request
    );
}
