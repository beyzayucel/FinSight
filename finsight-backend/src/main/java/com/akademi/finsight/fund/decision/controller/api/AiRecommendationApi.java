package com.akademi.finsight.fund.decision.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.decision.dto.request.FundDecisionRequest;
import com.akademi.finsight.fund.decision.dto.response.AIRecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(ApiEndpoints.Funds.BASE)
@Tag(name = "AI Recommendation", description = "AI portfolio decision recommendation operations")
public interface AiRecommendationApi {

    @Operation(summary = "Get pending AI recommendation",
            description = "Returns the latest pending AI recommendation for a fund. If none exists, a new one is generated.")
    @ApiResponse(responseCode = "200", description = "AI Recommendation retrieved successfully")
    @GetMapping("/{fundId}/recommendations/pending")
    ResponseEntity<ApiStandardResponse<AIRecommendationResponse>> getPendingRecommendation(
            @PathVariable UUID fundId,
            @AuthenticationPrincipal String email
    );

    @Operation(summary = "Submit decision on AI recommendation",
            description = "Submits user decision (ACCEPTED/REJECTED) on a pending AI recommendation.")
    @ApiResponse(responseCode = "200", description = "Decision submitted successfully")
    @PostMapping("/recommendations/{recommendationId}/decision")
    ResponseEntity<ApiStandardResponse<Void>> submitDecision(
            @PathVariable UUID recommendationId,
            @AuthenticationPrincipal String email,
            @Valid @RequestBody FundDecisionRequest request
    );
}

