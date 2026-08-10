package com.akademi.finsight.fund.decision.dto.request;

import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import jakarta.validation.constraints.NotNull;

public record FundDecisionRequest(
        @NotNull RecommendationStatus status,
        String note
) {}
