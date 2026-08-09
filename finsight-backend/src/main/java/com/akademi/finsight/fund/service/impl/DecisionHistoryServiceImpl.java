package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.dto.request.AttachStressTestRequest;
import com.akademi.finsight.fund.dto.response.DecisionRecordResponse;
import com.akademi.finsight.fund.dto.response.ManualScenarioResponse;
import com.akademi.finsight.fund.dto.response.ManualScenarioWeightResponse;
import com.akademi.finsight.fund.dto.response.PerformanceMetricsResponse;
import com.akademi.finsight.fund.entity.AiRecommendation;
import com.akademi.finsight.fund.entity.ManualScenario;
import com.akademi.finsight.fund.entity.PerformanceMetrics;
import com.akademi.finsight.fund.entity.RecommendationStatus;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.fund.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.repository.ManualScenarioRepository;
import com.akademi.finsight.fund.service.DecisionHistoryService;
import com.akademi.finsight.fund.service.ManualScenarioService;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import com.akademi.finsight.stresstest.mapper.StressTestResponseAssembler;
import com.akademi.finsight.stresstest.repository.StressTestResultRepository;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionHistoryServiceImpl implements DecisionHistoryService {

    private final ManualScenarioService manualScenarioService;
    private final ManualScenarioRepository manualScenarioRepository;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final StressTestResultRepository stressTestResultRepository;
    private final StressTestResponseAssembler stressTestResponseAssembler;
    private final UserService userService;

    @Override
    @Transactional
    public List<DecisionRecordResponse> getHistory(String email, UUID fundId) {
        User user = userService.findByEmail(email);

        Stream<DecisionRecordResponse> manualRecords = manualScenarioService.getScenarioHistory(email, fundId)
                                                                             .stream()
                                                                             .map(this::fromManualScenario);

        Stream<DecisionRecordResponse> aiRecords = aiRecommendationRepository
                .findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(user.getId(), fundId, RecommendationStatus.PENDING)
                .stream()
                .map(this::fromAiRecommendation);

        return Stream.concat(manualRecords, aiRecords)
                     .sorted(Comparator.comparing(DecisionRecordResponse::createdAt).reversed())
                     .toList();
    }

    @Override
    @Transactional
    public void attachStressTestResult(String email, AttachStressTestRequest request) {
        log.info("Attaching stress test result to latest decision. fundId: {}, stressTestResultId: {}",
                request.fundId(), request.stressTestResultId());

        User user = userService.findByEmail(email);

        StressTestResult stressTestResult = stressTestResultRepository.findById(request.stressTestResultId())
                                                                        .orElseThrow(() -> new StressTestException(StressTestErrorType.RESULT_NOT_FOUND));

        boolean belongsToUserAndFund = stressTestResult.getUser().getId().equals(user.getId())
                && stressTestResult.getFund().getId().equals(request.fundId());
        if (!belongsToUserAndFund) {
            throw new StressTestException(StressTestErrorType.RESULT_ACCESS_DENIED);
        }

        LatestDecision latest = findLatestDecision(user.getId(), request.fundId());

        if (latest.manualIsNewer()) {
            latest.manual().get().setStressTestResult(stressTestResult);
            manualScenarioRepository.save(latest.manual().get());
        } else if (latest.ai().isPresent()) {
            latest.ai().get().setStressTestResult(stressTestResult);
            aiRecommendationRepository.save(latest.ai().get());
        } else {
            throw new FundValidationException(FundErrorType.NO_DECISION_TO_ATTACH);
        }

        log.info("Stress test result attached successfully.");
    }

    private LatestDecision findLatestDecision(UUID userId, UUID fundId) {
        Optional<ManualScenario> manual = manualScenarioRepository
                .findFirstByUserIdAndFundIdOrderByCreatedAtDesc(userId, fundId);
        Optional<AiRecommendation> ai = aiRecommendationRepository
                .findFirstByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(userId, fundId, RecommendationStatus.PENDING);
        return new LatestDecision(manual, ai);
    }

    private record LatestDecision(Optional<ManualScenario> manual, Optional<AiRecommendation> ai) {
        boolean manualIsNewer() {
            return manual.isPresent()
                    && (ai.isEmpty() || createdAt(manual.get()).isAfter(createdAt(ai.get())));
        }

        private static Instant createdAt(ManualScenario scenario) {
            return scenario.getCreatedAt();
        }

        private static Instant createdAt(AiRecommendation recommendation) {
            return recommendation.getCreatedAt();
        }
    }

    private DecisionRecordResponse fromManualScenario(ManualScenarioResponse response) {
        return new DecisionRecordResponse(
                response.id(),
                "MANUAL",
                "ACCEPTED",
                null,
                response.note(),
                response.createdAt(),
                response.weights(),
                response.stockWeights(),
                response.metrics(),
                stressTestResponseAssembler.withLlmComment(response.stressTest())
        );
    }

    private DecisionRecordResponse fromAiRecommendation(AiRecommendation recommendation) {
        // K4: reddedilen kararda ağırlık gösterilmez, üretildiği anda kaydedilmiş olsa da.
        List<ManualScenarioWeightResponse> weights = recommendation.getStatus() == RecommendationStatus.ACCEPTED
                ? recommendation.getWeights().values().stream()
                                .map(w -> new ManualScenarioWeightResponse(w.getCategory(), w.getRecommendedWeight(), w.getCurrentWeight()))
                                .toList()
                : List.of();

        return new DecisionRecordResponse(
                recommendation.getId(),
                "AI",
                recommendation.getStatus().name(),
                recommendation.getRationale(),
                recommendation.getNote(),
                recommendation.getCreatedAt(),
                weights,
                // AI önerisi hisse bazında sinyal üretmiyor; tablo alanı da yok (AiRecommendationWeight
                // yalnızca kategori tutuyor). Motor bu granülerliğe geçtiğinde burası dolacak.
                List.of(),
                toMetricsResponse(recommendation.getMetrics()),
                stressTestResponseAssembler.toResponse(recommendation.getStressTestResult())
        );
    }

    private PerformanceMetricsResponse toMetricsResponse(PerformanceMetrics metrics) {
        if (metrics == null) return null;
        return new PerformanceMetricsResponse(
                metrics.getTotalReturnPct(),
                metrics.getBenchmarkDiffPct(),
                metrics.getMaxDrawdownPct(),
                metrics.getDailyVolatilityPct(),
                metrics.getAnalysisWindowDays()
        );
    }
}
