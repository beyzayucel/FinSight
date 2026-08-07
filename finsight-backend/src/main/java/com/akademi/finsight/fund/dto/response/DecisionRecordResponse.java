package com.akademi.finsight.fund.dto.response;

import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Manuel Senaryo ve AI Kararlarını tek, kronolojik sıralı bir listede birleştirir —
// Karar Geçmişi ekranı iki ayrı çağrı yapıp kendi tarafında merge etmek zorunda kalmasın diye.
public record DecisionRecordResponse(
        UUID id,
        String source,   // "AI" | "MANUAL"
        String status,   // "ACCEPTED" | "REJECTED"
        String rationale, // yalnızca source=AI için dolu
        String note,
        Instant createdAt,
        List<ManualScenarioWeightResponse> weights,
        PerformanceMetricsResponse metrics,
        StressTestInferenceResponseDto stressTest
) {}
