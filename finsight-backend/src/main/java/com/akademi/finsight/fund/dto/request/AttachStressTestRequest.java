package com.akademi.finsight.fund.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

// Stres Testi ekranındaki "Karar Geçmişine Kaydet" — mevcut stress test sonucunu
// o anki en güncel karara (manuel senaryo ya da AI kararı, hangisi daha yeniyse) iliştirir.
@Getter
@Setter
public class AttachStressTestRequest {

    @NotNull
    private UUID fundId;

    @NotNull
    private UUID stressTestResultId;
}
