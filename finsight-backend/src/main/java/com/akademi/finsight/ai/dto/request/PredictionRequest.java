package com.akademi.finsight.ai.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Yalnız legacy uyumluluk için. state, önceden ölçeklenmiş 16 elemanlı float dizisi olmalı. */
public record PredictionRequest(
        @NotNull @Size(min = 16, max = 16) List<BigDecimal> state
) {
}
