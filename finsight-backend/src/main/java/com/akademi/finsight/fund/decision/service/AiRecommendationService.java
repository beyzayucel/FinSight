package com.akademi.finsight.fund.decision.service;

import com.akademi.finsight.fund.decision.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;

import java.util.UUID;

public interface AiRecommendationService {
    AIRecommendationResponse getPendingRecommendation(UUID fundId, String email);
    void submitRecommendationDecision(UUID recommendationId, String email, RecommendationStatus status, String note);
}

