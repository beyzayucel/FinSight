package com.akademi.finsight.fund.performancecomparison.service.impl;

import com.akademi.finsight.fund.entity.AiRecommendation;
import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.entity.ManualScenario;
import com.akademi.finsight.fund.entity.RecommendationStatus;
import com.akademi.finsight.fund.performancecomparison.dto.response.ScenarioSource;
import com.akademi.finsight.fund.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.repository.ManualScenarioRepository;
import com.akademi.finsight.security.util.SecurityUtils;
import com.akademi.finsight.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScenarioResolver {

    private final FundRepository fundRepository;
    private final UserRepository userRepository;
    private final ManualScenarioRepository manualScenarioRepository;
    private final AiRecommendationRepository aiRecommendationRepository;

    public record ResolvedScenario(Map<AssetCategory, BigDecimal> weights, ScenarioSource source) {}

    public Optional<ResolvedScenario> resolve(String fundCode) {
        UUID fundId = fundRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("Fund not found: " + fundCode))
                .getId();

        String email = SecurityUtils.getCurrentUserEmail();
        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email))
                .getId();

        Optional<ManualScenario> manualOpt = manualScenarioRepository
                .findFirstByUserIdAndFundIdOrderByCreatedAtDesc(userId, fundId);

        Optional<AiRecommendation> aiOpt = aiRecommendationRepository
                .findLatestByFundAndUserAndStatus(fundId, email, RecommendationStatus.ACCEPTED);

        Instant manualAt = manualOpt.map(ManualScenario::getCreatedAt).orElse(Instant.MIN);
        Instant aiAt = aiOpt.map(AiRecommendation::getCreatedAt).orElse(Instant.MIN);

        if (manualAt.isAfter(aiAt) && manualOpt.isPresent()) {
            return Optional.of(new ResolvedScenario(
                    manualOpt.get().getSimulationWeights(), ScenarioSource.MANUAL));
        }

        return aiOpt.map(ai -> new ResolvedScenario(
                ai.getSimulationWeights(), ScenarioSource.AI));
    }
}
