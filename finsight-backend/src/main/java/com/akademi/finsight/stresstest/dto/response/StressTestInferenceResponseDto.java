package com.akademi.finsight.stresstest.dto.response;

import lombok.Builder;

@Builder(toBuilder = true)
public record StressTestInferenceResponseDto(
        String scenarioKey,
        PortfolioResultDto currentPortfolioResult,
        PortfolioResultDto simulationPortfolioResult,
        PortfolioResultDto benchmarkPortfolioResult,
        String llmComment

) {
}
