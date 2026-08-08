package com.akademi.finsight.stresstest.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record StressTestInferenceResponseDto(
        // Kaydedilen stress_test_results satırının id'si — Stres Testi ekranı bunu
        // "Karar Geçmişine Kaydet" akışında geri gönderir.
        UUID id,
        String scenarioKey,
        PortfolioResultDto currentPortfolioResult,
        PortfolioResultDto simulationPortfolioResult,
        PortfolioResultDto benchmarkPortfolioResult,
        String llmComment
) {
}
