package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.converter.FundDistributionConverter;
import com.akademi.finsight.fund.dto.MacroDataRow;
import com.akademi.finsight.fund.dto.request.FundModelInputRequest;
import com.akademi.finsight.fund.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.dto.response.FundActivePortfolioResponse;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.entity.*;
import com.akademi.finsight.fund.exception.AiRecommendationNotFoundException;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.mapper.AiRecommendationMapper;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioCurve;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.service.AiRecommendationService;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundService;
import com.akademi.finsight.fund.service.OnnxModelService;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private final FundProperties fundProperties;
    private final FundService fundService;
    private final FundDistributionService fundDistributionService;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final OnnxModelService onnxModelService;
    private final UserService userService;
    private final FundDistributionConverter fundDistributionConverter;
    private final AiRecommendationMapper aiRecommendationMapper;
    private final PortfolioSimulationCalculationService portfolioSimulationCalculationService;


    @Override
    @Transactional
    public AIRecommendationResponse getPendingRecommendation(UUID fundId, String email) {
        log.info("Fetching pending AI recommendation for fundId: {}, email: {}", fundId, MaskType.EMAIL.mask(email));

        User user = userService.findByEmail(email);
        FundResponse fund = fundService.getById(fundId);

        if (!fund.code().equals(fundProperties.getCode())) {
            throw new FundValidationException(FundErrorType.UNAUTHORIZED_RECOMMENDATION);
        }

        Optional<AiRecommendation> pendingRecommend = aiRecommendationRepository.findLatestByFundAndUserAndStatus(fundId, email, RecommendationStatus.PENDING);

        if (pendingRecommend.isPresent()) {
            AiRecommendation recommendation = pendingRecommend.get();
            Instant threshold = Instant.now().minus(fundProperties.getRecommendation().getTtlHours(), ChronoUnit.HOURS);

            if (recommendation.getCreatedAt().isBefore(threshold)) {
                log.info("Pending AI recommendation {} is older than {} hours. Soft-deleting it and generating a new one.", recommendation.getId(), fundProperties.getRecommendation().getTtlHours());
                recommendation.setDeleted(true);
                aiRecommendationRepository.save(recommendation);
            } else {
                log.info("Found valid pending AI recommendation: {}", recommendation.getId());
                return aiRecommendationMapper.toResponse(recommendation);
            }
        }

        return generateNewRecommendation(fund, user);
    }

    private AIRecommendationResponse generateNewRecommendation(FundResponse fund, User user) {
        log.info("Generating new AI recommendation for fund: {}, user: {}", fund.code(), MaskType.EMAIL.mask(user.getEmail()));

        FundModelInputRequest modelInput = createModelInput(fund);

        float[] recommendedWeights = predictWeights(modelInput);

        AiRecommendation recommendation = aiRecommendationMapper.toEntity(fund, user);

        AssetCategory[] categories = AssetCategory.values();

        for (int i = 0; i < categories.length; i++) {
            AssetCategory category = categories[i];

            addWeightToEntity(recommendation, category,
                    BigDecimal.valueOf(recommendedWeights[i] * 100),
                    BigDecimal.valueOf(getCurrentWeightByCategory(modelInput, category) * 100));
        }

        AiRecommendation saved = aiRecommendationRepository.save(recommendation);

        log.info("New AI recommendation saved. Recommendation ID: {}", saved.getId());

        return aiRecommendationMapper.toResponse(saved);
    }

    private List<FundDistributionResponse> getLatestDistributionsByFundCode(String fundCode) {
        return fundDistributionService.getLatestByFundCode(fundCode);
    }

    private FundModelInputRequest createModelInput(FundResponse fund) {
        List<FundDistributionResponse> distributions = getLatestDistributionsByFundCode(fund.code());

        Map<AssetCategory, BigDecimal> weights = fundDistributionConverter.toWeightsMapFromResponses(distributions);

        MacroDataRow macroRow = MacroDataFromDbMock.fetchRandom();

        return FundModelInputRequest.builder()
                .stockWeight(weights.getOrDefault(AssetCategory.STOCK, BigDecimal.ZERO).floatValue())
                .repoWeight(weights.getOrDefault(AssetCategory.REPO, BigDecimal.ZERO).floatValue())
                .futureWeight(weights.getOrDefault(AssetCategory.FUTURE, BigDecimal.ZERO).floatValue())
                .fundWeight(weights.getOrDefault(AssetCategory.FUND, BigDecimal.ZERO).floatValue())
                .usdReturn(macroRow.usdReturn().floatValue())
                .goldReturn(macroRow.goldReturn().floatValue())
                .brentReturn(macroRow.brentReturn().floatValue())
                .us10yReturn(macroRow.us10yReturn().floatValue())
                .cdsSpreadBps(macroRow.cdsSpreadBps().floatValue())
                .annualInflation(macroRow.annualInflation().floatValue())
                .policyRate(macroRow.policyRate().floatValue())
                .build();
    }

    private float[] predictWeights(FundModelInputRequest input) {
        float[] stateInput = {
                input.usdReturn(),
                input.goldReturn(),
                input.brentReturn(),
                input.us10yReturn(),
                input.cdsSpreadBps() / 1000.0f,
                input.annualInflation() / 100.0f,
                input.policyRate() / 100.0f,
                1.0f,
                1.0f,
                input.stockWeight(),
                input.repoWeight(),
                input.futureWeight(),
                input.fundWeight()
        };

        return onnxModelService.getAction(stateInput);
    }

    @Override
    @Transactional
    public void submitRecommendationDecision(UUID recommendationId, String email, RecommendationStatus status, String note) {
        log.info("Submitting decision for AI recommendation ID: {}, status: {}, user: {}", recommendationId, status, MaskType.EMAIL.mask(email));

        AiRecommendation recommendation = aiRecommendationRepository.findById(recommendationId)
                        .orElseThrow(AiRecommendationNotFoundException::new);

        if (!recommendation.getUser().getEmail().equals(email)) {
            throw new FundValidationException(FundErrorType.UNAUTHORIZED_RECOMMENDATION);
        }

        if (recommendation.getStatus() != RecommendationStatus.PENDING) {
            throw new FundValidationException(FundErrorType.RECOMMENDATION_ALREADY_PROCESSED);
        }

        recommendation.setStatus(status);
        recommendation.setNote(note);

        if (status == RecommendationStatus.ACCEPTED) {
            attachSimulationSnapshot(recommendation);
        }

        aiRecommendationRepository.save(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public FundActivePortfolioResponse getActiveFund() {
        log.info("Fetching active fund...");

        FundResponse fund = fundService.getByCode(fundProperties.getCode());

        if (Objects.isNull(fund)) {
            log.warn("No active fund found.");
            throw new FundNotFoundException();
        }

        List<FundDistributionResponse> distributions = fundDistributionService.getLatestByFundCode(fund.code());

        Map<AssetCategory, BigDecimal> weightsMap = fundDistributionConverter.toPercentageWeightsMap(distributions);

        LocalDate fundDate = distributions.isEmpty() ? LocalDate.now() : distributions.get(0).date();

        return new FundActivePortfolioResponse(fund.id(), fund.code(), fund.name(), fundDate, weightsMap);
    }


    //TODO: Consider refactoring this method
    private void attachSimulationSnapshot(AiRecommendation recommendation) {
        try {
            PortfolioCurve simulationCurve = portfolioSimulationCalculationService
                    .calculateSimulation(recommendation.getFund().getCode(), 30, recommendation.getSimulationWeights());
            if (simulationCurve != null) {
                recommendation.getMetrics().setSimulatedPortfolioValue(simulationCurve.metrics().currentValue());
                recommendation.getMetrics().setAnalysisWindowDays(30);
            }
        } catch (Exception e) {
            log.warn("Simulation snapshot failed for recommendation {}. Reason: {}",
                    recommendation.getId(), e.getMessage());
        }
    }

    private void addWeightToEntity(AiRecommendation aiRecommendation, AssetCategory assetCategory, BigDecimal recommended, BigDecimal current) {
        AiRecommendationWeight weight = AiRecommendationWeight.builder()
                .category(assetCategory)
                .recommendedWeight(recommended.setScale(2, RoundingMode.HALF_UP))
                .currentWeight(current.setScale(2, RoundingMode.HALF_UP))
                .build();
        aiRecommendation.addWeight(weight);
    }

    private double getCurrentWeightByCategory(FundModelInputRequest input, AssetCategory category) {
        return switch (category) {
            case STOCK -> input.stockWeight();
            case REPO -> input.repoWeight();
            case FUTURE -> input.futureWeight();
            case FUND -> input.fundWeight();
        };
    }
}
