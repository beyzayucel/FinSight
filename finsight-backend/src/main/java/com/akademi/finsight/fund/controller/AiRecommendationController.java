package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.controller.api.AiRecommendationApi;
import com.akademi.finsight.fund.dto.request.FundDecisionRequest;
import com.akademi.finsight.fund.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.dto.response.FundActivePortfolioResponse;
import com.akademi.finsight.fund.service.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AiRecommendationController extends BaseController implements AiRecommendationApi {

    private final AiRecommendationService aiRecommendationService;

    @Override
    public ResponseEntity<ApiStandardResponse<AIRecommendationResponse>> getPendingRecommendation(UUID fundId, String email) {
        return ok(aiRecommendationService.getPendingRecommendation(fundId, email));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundActivePortfolioResponse>> getActiveFund() {
        return ok(aiRecommendationService.getActiveFund());
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> submitDecision(UUID recommendationId, String email, FundDecisionRequest request) {
        aiRecommendationService.submitRecommendationDecision(recommendationId, email, request.status(), request.note());
        return ok();
    }
}

