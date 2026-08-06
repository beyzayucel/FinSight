package com.akademi.finsight.stresstest.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record PortfolioDataDto(

        @NotNull(message = "{error.validation.portfolio.initial_value.not_null}")
        @Positive(message = "{error.validation.portfolio.initial_value.positive}")
        BigDecimal initialValue,

        @NotEmpty(message = "{error.validation.portfolio.asset_weights.not_empty}")
        Map<String, Float> assetWeights
) {}

