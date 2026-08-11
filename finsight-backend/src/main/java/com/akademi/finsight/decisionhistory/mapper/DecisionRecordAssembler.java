package com.akademi.finsight.decisionhistory.mapper;

import com.akademi.finsight.fund.decision.dto.response.ManualScenarioResponse;
import com.akademi.finsight.fund.decision.dto.response.ManualScenarioStockWeightResponse;
import com.akademi.finsight.fund.decision.dto.response.ManualScenarioWeightResponse;
import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.ManualScenario;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import com.akademi.finsight.fund.decision.mapper.ManualScenarioMapper;
import com.akademi.finsight.decisionhistory.dto.response.DecisionRecordResponse;
import com.akademi.finsight.fund.dto.response.PerformanceMetricsResponse;
import com.akademi.finsight.fund.entity.PerformanceMetrics;
import com.akademi.finsight.stresstest.mapper.StressTestResponseAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds the unified {@link DecisionRecordResponse} out of an AI recommendation or a manual
 * scenario.
 *
 * <p>Two screens need the exact same record: the portfolio manager's own Decision History and the
 * Admin Panel's decision detail. They differ only in how the decision is looked up (by e-mail scope
 * vs. by id), so the mapping lives here instead of being duplicated in both services.
 */
@Component
@RequiredArgsConstructor
public class DecisionRecordAssembler {

    private static final String SOURCE_AI = "AI";
    private static final String SOURCE_MANUAL = "MANUAL";

    private final ManualScenarioMapper manualScenarioMapper;
    private final StressTestResponseAssembler stressTestResponseAssembler;

    public DecisionRecordResponse fromManualScenario(ManualScenario scenario) {
        return fromManualScenario(manualScenarioMapper.toResponse(scenario));
    }

    public DecisionRecordResponse fromManualScenario(ManualScenarioResponse response) {
        return new DecisionRecordResponse(
                response.id(),
                SOURCE_MANUAL,
                RecommendationStatus.ACCEPTED.name(),
                null,
                response.note(),
                response.createdAt(),
                response.weights(),
                response.stockWeights(),
                response.metrics(),
                stressTestResponseAssembler.withLlmComment(response.stressTest())
        );
    }

    public DecisionRecordResponse fromAiRecommendation(AiRecommendation recommendation) {
        boolean accepted = recommendation.getStatus() == RecommendationStatus.ACCEPTED;

        // K4: reddedilen kararda ağırlık gösterilmez, üretildiği anda kaydedilmiş olsa da.
        List<ManualScenarioWeightResponse> weights = accepted && recommendation.getWeights() != null
                ? recommendation.getWeights().values().stream()
                                .map(w -> new ManualScenarioWeightResponse(w.getCategory(), w.getRecommendedWeight(), w.getCurrentWeight()))
                                .toList()
                : List.of();

        List<ManualScenarioStockWeightResponse> stockWeights = accepted && recommendation.getStockWeights() != null
                ? recommendation.getStockWeights().values().stream()
                                .map(w -> new ManualScenarioStockWeightResponse(w.getAssetCode(), w.getRecommendedWeight(), w.getCurrentWeight()))
                                .toList()
                : List.of();

        return new DecisionRecordResponse(
                recommendation.getId(),
                SOURCE_AI,
                recommendation.getStatus().name(),
                recommendation.getRationale(),
                recommendation.getNote(),
                recommendation.getCreatedAt(),
                weights,
                stockWeights,
                toMetricsResponse(recommendation.getMetrics()),
                stressTestResponseAssembler.toResponse(recommendation.getStressTestResult())
        );
    }

    private PerformanceMetricsResponse toMetricsResponse(PerformanceMetrics metrics) {
        if (metrics == null) {
            return null;
        }
        return new PerformanceMetricsResponse(
                metrics.getTotalReturnPct(),
                metrics.getBenchmarkDiffPct(),
                metrics.getMaxDrawdownPct(),
                metrics.getDailyVolatilityPct(),
                metrics.getAnalysisWindowDays(),
                metrics.getDataDate()
        );
    }
}
