package com.akademi.finsight.fund.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Stres Testi ekranındaki "Karar Geçmişine Kaydet" — mevcut stress test sonucunu
// o anki en güncel karara (manuel senaryo ya da AI kararı, hangisi daha yeniyse) iliştirir.
public record AttachStressTestRequest(

        @NotNull
        UUID fundId,

        @NotNull
        UUID stressTestResultId
) {}
