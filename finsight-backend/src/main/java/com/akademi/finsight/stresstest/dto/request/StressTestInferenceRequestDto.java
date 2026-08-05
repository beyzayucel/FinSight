package com.akademi.finsight.stresstest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record StressTestInferenceRequestDto(

        @NotBlank(message = "Senaryo anahtarı boş olamaz (Örn: HISSE_SOKU, FAIZ_SOKU).")
        String scenarioKey,

        @Valid
        @NotNull(message = "Mevcut portföy verisi zorunludur.")
        PortfolioDataDto currentPortfolio,

        @Valid
        @NotNull(message = "Simülasyon portföy verisi zorunludur.")
        PortfolioDataDto simulationPortfolio,

        @Valid
        @NotNull(message = "Benchmark portföy verisi zorunludur.")
        PortfolioDataDto benchmarkPortfolio
) {}