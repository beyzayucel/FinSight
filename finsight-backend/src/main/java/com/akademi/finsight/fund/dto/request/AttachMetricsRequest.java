package com.akademi.finsight.fund.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

// Performans Karşılaştırması simülasyonu hesapladıktan hemen sonra, o kullanıcı+fon için
// en güncel karara (Manuel senaryo ya da AI önerisi, hangisi daha yeniyse) performans
// özetini iliştirmek için — apply/decision akışlarına dokunmadan, ayrı bir çağrı.
@Getter
@Setter
public class AttachMetricsRequest {

    @NotNull
    private UUID fundId;

    private BigDecimal totalReturnPct;
    private BigDecimal benchmarkDiffPct;
    private BigDecimal maxDrawdownPct;
    private BigDecimal dailyVolatilityPct;
    private Integer analysisWindowDays;
}
