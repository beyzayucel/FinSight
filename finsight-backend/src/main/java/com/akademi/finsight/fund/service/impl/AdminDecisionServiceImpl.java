package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.dto.response.AdminDecisionRecordResponse;
import com.akademi.finsight.fund.entity.AiRecommendation;
import com.akademi.finsight.fund.entity.DecisionType;
import com.akademi.finsight.fund.entity.ManualScenario;
import com.akademi.finsight.fund.entity.RecommendationStatus;
import com.akademi.finsight.fund.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.repository.AiRecommendationSpecification;
import com.akademi.finsight.fund.repository.ManualScenarioRepository;
import com.akademi.finsight.fund.repository.ManualScenarioSpecification;
import com.akademi.finsight.fund.service.AdminDecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDecisionServiceImpl implements AdminDecisionService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final AiRecommendationRepository aiRecommendationRepository;
    private final ManualScenarioRepository manualScenarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminDecisionRecordResponse> getDecisionReport(UUID userId, DecisionType type, Integer days) {
        log.info("Fetching admin decision report. userId: {}, type: {}, days: {}", userId, type, days);

        Instant since = days == null ? null : Instant.now().minus(days, ChronoUnit.DAYS);

        List<AdminDecisionRecordResponse> report = Stream.concat(
                                                                 findAiRecommendations(userId, type, since),
                                                                 findManualScenarios(userId, type, since))
                                                         .sorted(Comparator.comparing(AdminDecisionRecordResponse::decisionDate).reversed())
                                                         .toList();

        log.info("Admin decision report retrieved. recordCount: {}", report.size());
        return report;
    }

    private Stream<AdminDecisionRecordResponse> findAiRecommendations(UUID userId, DecisionType type, Instant since) {
        if (type == DecisionType.MANUAL) {
            return Stream.empty();
        }

        Specification<AiRecommendation> spec = AiRecommendationSpecification.decided()
                                                                            .and(AiRecommendationSpecification.withUser(userId))
                                                                            .and(AiRecommendationSpecification.createdSince(since));

        return aiRecommendationRepository.findAll(spec, NEWEST_FIRST)
                                          .stream()
                                          .filter(recommendation -> matchesAiType(recommendation, type))
                                          .map(this::fromAiRecommendation);
    }

    private Stream<AdminDecisionRecordResponse> findManualScenarios(UUID userId, DecisionType type, Instant since) {
        if (type == DecisionType.AI_APPROVED || type == DecisionType.AI_REJECTED) {
            return Stream.empty();
        }

        Specification<ManualScenario> spec = ManualScenarioSpecification.withUser(userId)
                                                                        .and(ManualScenarioSpecification.createdSince(since));

        return manualScenarioRepository.findAll(spec, NEWEST_FIRST)
                                        .stream()
                                        .map(this::fromManualScenario);
    }

    private boolean matchesAiType(AiRecommendation recommendation, DecisionType type) {
        return type == null || toDecisionType(recommendation) == type;
    }

    private DecisionType toDecisionType(AiRecommendation recommendation) {
        return recommendation.getStatus() == RecommendationStatus.ACCEPTED
                ? DecisionType.AI_APPROVED
                : DecisionType.AI_REJECTED;
    }

    private AdminDecisionRecordResponse fromAiRecommendation(AiRecommendation recommendation) {
        return new AdminDecisionRecordResponse(
                recommendation.getId(),
                recommendation.getCreatedAt(),
                recommendation.getUser().getFullName(),
                recommendation.getFund().getName(),
                toDecisionType(recommendation)
        );
    }

    private AdminDecisionRecordResponse fromManualScenario(ManualScenario scenario) {
        return new AdminDecisionRecordResponse(
                scenario.getId(),
                scenario.getCreatedAt(),
                scenario.getUser().getFullName(),
                scenario.getFund().getName(),
                DecisionType.MANUAL
        );
    }
}
