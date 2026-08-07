package com.akademi.finsight.fund.dto.response;

import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ManualScenarioResponse(
        UUID id,
        UUID fundId,
        String note,
        Instant createdAt,
        List<ManualScenarioWeightResponse> weights,
        PerformanceMetricsResponse metrics,
        StressTestInferenceResponseDto stressTest
) {}
