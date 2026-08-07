package com.akademi.finsight.fund.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

// Karar Geçmişi'nde her karar için gösterilen performans özeti — hesaplama
// frontend'deki simülasyon motorunda yapılır (mock/sentetik), backend sadece
// sonucu ManualScenario/AiDecision üzerinde saklar. Nullable: kayıt oluşturulduğunda
// henüz bilinmeyebilir (bkz. manuel senaryo için ayrı "metrik ekle" akışı).
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PerformanceMetrics {

    @Column(name = "total_return_pct", precision = 6, scale = 2)
    private BigDecimal totalReturnPct;

    @Column(name = "benchmark_diff_pct", precision = 6, scale = 2)
    private BigDecimal benchmarkDiffPct;

    @Column(name = "max_drawdown_pct", precision = 6, scale = 2)
    private BigDecimal maxDrawdownPct;

    @Column(name = "daily_volatility_pct", precision = 6, scale = 2)
    private BigDecimal dailyVolatilityPct;

    // Metriklerin hesaplandığı andaki analiz penceresi (10/20/30/90 gün) —
    // Karar Geçmişi özet satırındaki "Süre: X işlem günü" için.
    @Column(name = "analysis_window_days")
    private Integer analysisWindowDays;
}
