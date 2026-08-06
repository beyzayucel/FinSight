package com.akademi.finsight.stresstest.dto.response;

public record StressTestInferenceResponseDto(
        String scenarioKey,
        PortfolioResultDto currentPortfolioResult,
        PortfolioResultDto simulationPortfolioResult,
        PortfolioResultDto benchmarkPortfolioResult
) {
}
