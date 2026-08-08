package com.akademi.finsight.stresstest.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Stres Testi ekranındaki "Karar Geçmişine Kaydet" — /run ile hesaplanıp kaydedilmiş bir
// stres testi sonucunu, o fondaki en güncel karara (manuel senaryo ya da AI kararı) iliştirir.
// Sayılar zaten /run sırasında kaydedildiği için burada yeniden portföy verisi taşınmaz.
public record SaveStressTestDecisionRequestDto(

        @NotNull(message = "{error.validation.stresstest.fund_id.not_null}")
        UUID fundId,

        @NotNull(message = "{error.validation.stresstest.result_id.not_null}")
        UUID stressTestResultId
) {}
