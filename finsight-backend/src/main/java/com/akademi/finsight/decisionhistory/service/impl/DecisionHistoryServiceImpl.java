package com.akademi.finsight.decisionhistory.service.impl;

import com.akademi.finsight.decisionhistory.dto.request.AttachStressTestRequest;
import com.akademi.finsight.decisionhistory.dto.response.DecisionRecordResponse;
import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.ManualScenario;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import com.akademi.finsight.decisionhistory.mapper.DecisionRecordAssembler;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.fund.decision.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.decision.repository.ManualScenarioRepository;
import com.akademi.finsight.decisionhistory.service.DecisionHistoryService;
import com.akademi.finsight.fund.constant.CacheNames;
import com.akademi.finsight.fund.decision.service.ManualScenarioService;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import com.akademi.finsight.stresstest.repository.StressTestResultRepository;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final DecisionRecordAssembler decisionRecordAssembler;
    private final UserService userService;

    @Override
    @Transactional
    @Cacheable(cacheManager = "caffeineCacheManager", cacheNames = CacheNames.DECISION_HISTORY)
    public List<DecisionRecordResponse> getHistory(String email, UUID fundId) {
        User user = userService.findByEmail(email);

        Stream<DecisionRecordResponse> manualRecords = manualScenarioService.getScenarioHistory(email, fundId)
                                                                             .stream()
                                                                             .map(decisionRecordAssembler::fromManualScenario);

        Stream<DecisionRecordResponse> aiRecords = aiRecommendationRepository
                .findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(user.getId(), fundId, RecommendationStatus.PENDING)
                .stream()
                .map(decisionRecordAssembler::fromAiRecommendation);

        return Stream.concat(manualRecords, aiRecords)
                     .sorted(Comparator.comparing(DecisionRecordResponse::createdAt).reversed())
                     .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheManager = "caffeineCacheManager", cacheNames = CacheNames.DECISION_HISTORY, allEntries = true)
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

}
