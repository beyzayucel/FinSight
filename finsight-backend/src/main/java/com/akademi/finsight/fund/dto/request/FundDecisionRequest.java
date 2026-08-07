package com.akademi.finsight.fund.dto.request;

import com.akademi.finsight.fund.entity.RecommendationStatus;
import jakarta.validation.constraints.NotNull;

public record FundDecisionRequest(
        @NotNull RecommendationStatus status,
        String note
) {}
