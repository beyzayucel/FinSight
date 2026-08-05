package com.akademi.finsight.stresstest.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record PortfolioDataDto(

        @NotNull(message = "Şok öncesi portföy değeri boş olamaz.")
        @Positive(message = "Portföy değeri pozitif olmalıdır.")
        BigDecimal initialValue,

        /**
         * Varlık Tipleri ve Ağırlıkları (Toplamı 1.0 / %100 olmalıdır)
         * Örn: {"EQUITY": 0.40, "BOND": 0.30, "FX": 0.20, "CASH": 0.10}
         */
        @NotEmpty(message = "Varlık ağırlıkları boş olamaz.")
        Map<String, Float> assetWeights
) {}

