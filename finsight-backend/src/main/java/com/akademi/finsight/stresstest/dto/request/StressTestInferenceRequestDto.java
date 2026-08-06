package com.akademi.finsight.stresstest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record StressTestInferenceRequestDto(

        @NotBlank(message = "{error.validation.stresstest.scenario_key.not_blank}")
        String scenarioKey,

        @Valid
        @NotNull(message = "{error.validation.stresstest.current_portfolio.not_null}")
        PortfolioDataDto currentPortfolio,

        @Valid
        @NotNull(message = "{error.validation.stresstest.simulation_portfolio.not_null}")
        PortfolioDataDto simulationPortfolio,

        @Valid
        @NotNull(message = "{error.validation.stresstest.benchmark_portfolio.not_null}")
        PortfolioDataDto benchmarkPortfolio
) {}