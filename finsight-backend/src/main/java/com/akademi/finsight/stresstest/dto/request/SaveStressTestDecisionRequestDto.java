package com.akademi.finsight.stresstest.dto.request;

import com.akademi.finsight.stresstest.enums.SimulationType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record SaveStressTestDecisionRequestDto(
        @NotNull(message = "{validation.refresh.token.required}")
        String fundId,

        @NotNull(message = "Senaryo türü zorunludur.")
        SimulationType scenarioKey,

        @NotNull(message = "Portföy verisi zorunludur.")
        PortfolioDataDto portfolioData,

        String llmComment
) {}
