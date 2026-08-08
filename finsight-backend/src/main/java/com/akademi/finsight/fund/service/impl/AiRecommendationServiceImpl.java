package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.constant.MacroConstants;
import com.akademi.finsight.fund.converter.FundDistributionConverter;
import com.akademi.finsight.fund.dto.MacroDataRow;
import com.akademi.finsight.fund.dto.request.FundModelInputRequest;
import com.akademi.finsight.fund.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.entity.*;
import com.akademi.finsight.fund.exception.AiRecommendationNotFoundException;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.mapper.AiRecommendationMapper;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.repository.MacroDataRepository;
import com.akademi.finsight.fund.service.AiRecommendationService;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.fund.service.FundService;
import com.akademi.finsight.fund.service.OnnxModelService;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.service.InfinaService;
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
    private final FundPeriodMetricService fundPeriodMetricService;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final OnnxModelService onnxModelService;
    private final UserService userService;
    private final FundDistributionConverter fundDistributionConverter;
    private final AiRecommendationMapper aiRecommendationMapper;
    private final MacroDataRepository macroDataRepository;
    private final InfinaService infinaService;
    private final PortfolioSimulationCalculationService portfolioSimulationCalculationService;


    @Override
    @Transactional
    public AIRecommendationResponse getPendingRecommendation(UUID fundId, String email) {
        log.info("Fetching pending AI recommendation for fundId: {}, email: {}", fundId, MaskType.EMAIL.mask(email));

        User user = userService.findByEmail(email);
        FundResponse fund = fundService.getById(fundId);

        Optional<AiRecommendation> pendingRecommend = aiRecommendationRepository.findLatestByFundAndUserAndStatus(fundId, email, RecommendationStatus.PENDING);

        if (pendingRecommend.isPresent()) {
            AiRecommendation recommendation = pendingRecommend.get();
            long ttlHours = fundProperties.getRecommendation().getTtlHours();
            Instant threshold = Instant.now().minus(ttlHours > 0 ? ttlHours : 24, ChronoUnit.HOURS);

            if (recommendation.getCreatedAt().isBefore(threshold)) {
                log.info("Pending AI recommendation {} is older than {} hours. Soft-deleting it and generating a new one.", recommendation.getId(), ttlHours);
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
                    getCurrentWeightByCategory(modelInput, category).multiply(BigDecimal.valueOf(100)));
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

        MacroDataRow macroRow = macroDataRepository.findFirstByOrderByDateDesc()
                .map(macroData -> new MacroDataRow(
                        macroData.getDate(),
                        macroData.getUsdReturn(),
                        macroData.getGoldReturn(),
                        macroData.getBrentReturn(),
                        macroData.getUs10yReturn(),
                        macroData.getCdsSpreadBps(),
                        macroData.getAnnualInflation(),
                        macroData.getPolicyRate()
                ))
                .orElseGet(() -> {
                    log.warn("MacroData table is empty, falling back to safe default values.");
                    return new MacroDataRow(
                            LocalDate.now(),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            MacroConstants.DEFAULT_CDS,
                            MacroConstants.DEFAULT_INFLATION,
                            MacroConstants.DEFAULT_POLICY_RATE
                    );
                });

        List<FundPeriodMetricResponse> metrics = fundPeriodMetricService.getLatestByFundCode(fund.code());
        BigDecimal fundReturn = BigDecimal.ZERO;
        BigDecimal portfolioGrowth = BigDecimal.ZERO;
        BigDecimal portfolioValue = BigDecimal.ZERO;
        BigDecimal activeValue = BigDecimal.ZERO;
        BigDecimal cashValue = BigDecimal.ZERO;
        BigDecimal investorCount = BigDecimal.ZERO;

        if (metrics != null && !metrics.isEmpty()) {
            FundPeriodMetricResponse latestMetric = metrics.getFirst();
            if (latestMetric.dailyReturn() != null) {
                fundReturn = latestMetric.dailyReturn();
            }
            if (latestMetric.totalValue() != null) {
                portfolioValue = latestMetric.totalValue();
                activeValue = latestMetric.totalValue();
            }
            if (latestMetric.totalValue() != null && latestMetric.previousTotalValue() != null
                    && latestMetric.previousTotalValue().compareTo(BigDecimal.ZERO) > 0) {
                portfolioGrowth = latestMetric.totalValue()
                        .subtract(latestMetric.previousTotalValue())
                        .divide(latestMetric.previousTotalValue(), 12, RoundingMode.HALF_UP);
            }
        }

        try {
            FundInfoResponse fundInfo = infinaService.getFundInfo(fund.code(), null, null);
            if (fundInfo != null) {
                if (fundInfo.investorCount() != null) {
                    investorCount = BigDecimal.valueOf(fundInfo.investorCount());
                }
                if (fundInfo.totalMarketPrice() != null) {
                    portfolioValue = fundInfo.totalMarketPrice();
                    activeValue = fundInfo.totalMarketPrice();
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch additional fund info for {}: {}", fund.code(), e.getMessage());
        }

        return FundModelInputRequest.builder()
                .stockWeight(weights.getOrDefault(AssetCategory.STOCK, BigDecimal.ZERO))
                .repoWeight(weights.getOrDefault(AssetCategory.REPO, BigDecimal.ZERO))
                .futureWeight(weights.getOrDefault(AssetCategory.FUTURE, BigDecimal.ZERO))
                .fundWeight(weights.getOrDefault(AssetCategory.FUND, BigDecimal.ZERO))
                .usdReturn(macroRow.usdReturn())
                .goldReturn(macroRow.goldReturn())
                .brentReturn(macroRow.brentReturn())
                .us10yReturn(macroRow.us10yReturn())
                .cdsSpreadBps(macroRow.cdsSpreadBps())
                .annualInflation(macroRow.annualInflation())
                .policyRate(macroRow.policyRate())
                .fundReturn(fundReturn)
                .portfolioGrowth(portfolioGrowth)
                .activeValue(activeValue)
                .portfolioValue(portfolioValue)
                .cashValue(cashValue)
                .investorCount(investorCount)
                .build();
    }

    private float[] predictWeights(FundModelInputRequest input) {
        float[] stateInput = {
                input.usdReturn().floatValue(),
                input.goldReturn().floatValue(),
                input.brentReturn().floatValue(),
                input.us10yReturn().floatValue(),
                input.cdsSpreadBps().floatValue() / 1000.0f,
                input.annualInflation().floatValue() / 100.0f,
                input.policyRate().floatValue() / 100.0f,
                input.fundReturn() != null ? input.fundReturn().floatValue() : 0.0f,
                input.portfolioGrowth() != null ? input.portfolioGrowth().floatValue() : 0.0f,
                input.activeValue() != null ? input.activeValue().floatValue() : 0.0f,
                input.portfolioValue() != null ? input.portfolioValue().floatValue() : 0.0f,
                input.cashValue() != null ? input.cashValue().floatValue() : 0.0f,
                input.investorCount() != null ? input.investorCount().floatValue() : 0.0f,
                input.stockWeight().floatValue(),
                input.repoWeight().floatValue(),
                input.futureWeight().floatValue(),
                input.fundWeight().floatValue()
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
            portfolioSimulationCalculationService.attachSnapshot(
                    recommendation, recommendation.getFund().getCode(), 30, recommendation.getSimulationWeights());
        }

        aiRecommendationRepository.save(recommendation);
    }

    private void addWeightToEntity(AiRecommendation aiRecommendation, AssetCategory assetCategory, BigDecimal recommended, BigDecimal current) {
        AiRecommendationWeight weight = AiRecommendationWeight.builder()
                .category(assetCategory)
                .recommendedWeight(recommended.setScale(2, RoundingMode.HALF_UP))
                .currentWeight(current.setScale(2, RoundingMode.HALF_UP))
                .build();
        aiRecommendation.addWeight(weight);
    }

    private BigDecimal getCurrentWeightByCategory(FundModelInputRequest input, AssetCategory category) {
        return switch (category) {
            case STOCK -> input.stockWeight();
            case REPO -> input.repoWeight();
            case FUTURE -> input.futureWeight();
            case FUND -> input.fundWeight();
        };
    }
}
