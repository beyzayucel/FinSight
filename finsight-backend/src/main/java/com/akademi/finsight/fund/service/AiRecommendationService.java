package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.dto.response.FundActivePortfolioResponse;
import com.akademi.finsight.fund.entity.RecommendationStatus;

import java.util.UUID;

public interface AiRecommendationService {
    AIRecommendationResponse getPendingRecommendation(UUID fundId, String email);
    void submitRecommendationDecision(UUID recommendationId, String email, RecommendationStatus status, String note);
    FundActivePortfolioResponse getActiveFund();
}

