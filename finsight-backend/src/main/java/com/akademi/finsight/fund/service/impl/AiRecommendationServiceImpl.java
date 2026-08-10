package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.constant.CacheNames;
import com.akademi.finsight.fund.constant.MarketConstants;
import com.akademi.finsight.fund.converter.FundDistributionConverter;
import com.akademi.finsight.fund.dto.request.MarketDataRequest;
import com.akademi.finsight.fund.dto.request.FundModelInputRequest;
import com.akademi.finsight.fund.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.entity.*;
import com.akademi.finsight.fund.exception.AiRecommendationNotFoundException;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.fund.mapper.AiRecommendationMapper;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.repository.FundPriceDataRepository;
import com.akademi.finsight.fund.repository.MarketDataRepository;
import com.akademi.finsight.fund.service.AiRecommendationService;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.fund.service.FundService;
import com.akademi.finsight.fund.service.FundStockAllocationService;
import com.akademi.finsight.fund.service.OnnxModelService;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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

    private static final float CDS_NORMALIZATION_FACTOR = 1000.0f;
    private static final float PERCENT_NORMALIZATION_FACTOR = 100.0f;
    private static final float BILLION_SCALE = 1_000_000_000.0f;
    private static final float MILLION_SCALE = 1_000_000.0f;
    private static final float INVESTOR_COUNT_SCALE = 100_000.0f;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final String OTHERS_ASSET_CODE = "Others";
    private static final String OTHERS_ASSET_CODE_TR = "Diğer";

    private final FundProperties fundProperties;
    private final FundService fundService;
    private final FundDistributionService fundDistributionService;
    private final FundStockAllocationService fundStockAllocationService;
    private final FundPeriodMetricService fundPeriodMetricService;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final OnnxModelService onnxModelService;
    private final UserService userService;
    private final FundDistributionConverter fundDistributionConverter;
    private final AiRecommendationMapper aiRecommendationMapper;
    private final MarketDataRepository marketDataRepository;
    private final FundPriceDataRepository fundPriceDataRepository;
    private final PortfolioSimulationCalculationService portfolioSimulationCalculationService;

    @Override
    @Transactional
    public AIRecommendationResponse getPendingRecommendation(UUID fundId, String email) {
        log.info("Fetching pending AI recommendation for fundId: {}, email: {}", fundId, MaskType.EMAIL.mask(email));

        User user = userService.findByEmail(email);
        FundResponse fund = fundService.getById(fundId);

        Optional<AiRecommendation> pendingRecommend = aiRecommendationRepository
                .findLatestByFundAndUserAndStatus(fundId, email, RecommendationStatus.PENDING);

        if (pendingRecommend.isPresent()) {
            AiRecommendation recommendation = pendingRecommend.get();
            if (isRecommendationExpired(recommendation)) {
                expireRecommendation(recommendation);
            } else {
                log.info("Found valid pending AI recommendation: {}", recommendation.getId());
                return aiRecommendationMapper.toResponse(recommendation);
            }
        }

        return generateNewRecommendation(fund, user);
    }

    private boolean isRecommendationExpired(AiRecommendation recommendation) {
        long ttlHours = fundProperties.getRecommendation().getTtlHours();
        Instant threshold = Instant.now().minus(ttlHours, ChronoUnit.HOURS);
        return recommendation.getCreatedAt().isBefore(threshold);
    }

    private void expireRecommendation(AiRecommendation recommendation) {
        long ttlHours = fundProperties.getRecommendation().getTtlHours();
        log.info("Pending AI recommendation {} is older than {} hours. Soft-deleting it and generating a new one.", recommendation.getId(), ttlHours);
        recommendation.setDeleted(true);
        aiRecommendationRepository.save(recommendation);
    }

    private AIRecommendationResponse generateNewRecommendation(FundResponse fund, User user) {
        log.info("Generating new AI recommendation for fund: {}, user: {}", fund.code(), MaskType.EMAIL.mask(user.getEmail()));

        FundModelInputRequest modelInput = createModelInput(fund);
        float[] recommendedWeights = predictWeights(modelInput);

        AiRecommendation recommendation = aiRecommendationMapper.toEntity(fund, user);
        populateRecommendationWeights(recommendation, recommendedWeights, modelInput);
        populateRecommendationStockWeights(recommendation, fund.code());

        AiRecommendation saved = aiRecommendationRepository.save(recommendation);
        log.info("New AI recommendation saved. Recommendation ID: {}", saved.getId());

        return aiRecommendationMapper.toResponse(saved);
    }

    private void populateRecommendationWeights(AiRecommendation recommendation, float[] recommendedWeights, FundModelInputRequest modelInput) {
        AssetCategory[] categories = AssetCategory.values();
        for (int i = 0; i < categories.length; i++) {
            AssetCategory category = categories[i];
            BigDecimal recommended = BigDecimal.valueOf(recommendedWeights[i]).multiply(HUNDRED);
            BigDecimal current = getWeight(modelInput, category).multiply(HUNDRED);

            addWeightToEntity(recommendation, category, recommended, current);
        }
    }

    private void populateRecommendationStockWeights(AiRecommendation recommendation, String fundCode) {
        var breakdown = fundStockAllocationService.getBreakdownByFundCode(fundCode, null);
        if (breakdown == null || breakdown.items() == null || breakdown.items().isEmpty()) {
            return;
        }

        List<com.akademi.finsight.fund.dto.response.FundStockWeightResponse> items = breakdown.items();
        BigDecimal othersWeight = BigDecimal.ZERO;
        List<com.akademi.finsight.fund.dto.response.FundStockWeightResponse> activeStocks = new ArrayList<>();

        for (var item : items) {
            if (OTHERS_ASSET_CODE.equalsIgnoreCase(item.assetCode()) || OTHERS_ASSET_CODE_TR.equalsIgnoreCase(item.assetCode())) {
                othersWeight = item.weight();
            } else {
                activeStocks.add(item);
            }
        }

        BigDecimal activeTargetTotal = HUNDRED.subtract(othersWeight);
        BigDecimal accumulatedRecommended = BigDecimal.ZERO;

        for (int i = 0; i < activeStocks.size(); i++) {
            var stock = activeStocks.get(i);
            BigDecimal current = stock.weight();
            BigDecimal recommended;

            if (i == activeStocks.size() - 1) {
                recommended = activeTargetTotal.subtract(accumulatedRecommended);
            } else {
                double tilt = Math.sin(i * 1.3) * 0.8;
                BigDecimal adjusted = current.add(BigDecimal.valueOf(tilt)).setScale(2, RoundingMode.HALF_UP);
                if (adjusted.compareTo(BigDecimal.ZERO) < 0) adjusted = current;
                recommended = adjusted;
                accumulatedRecommended = accumulatedRecommended.add(recommended);
            }

            AiRecommendationStockWeight stockWeight = aiRecommendationMapper.toStockWeightEntity(
                    stock.assetCode(), recommended, current, recommendation);
            recommendation.addStockWeight(stockWeight);
        }

        if (othersWeight.compareTo(BigDecimal.ZERO) > 0 || items.stream().anyMatch(it -> OTHERS_ASSET_CODE.equalsIgnoreCase(it.assetCode()) || OTHERS_ASSET_CODE_TR.equalsIgnoreCase(it.assetCode()))) {
            AiRecommendationStockWeight othersWeightEntity = aiRecommendationMapper.toStockWeightEntity(
                    OTHERS_ASSET_CODE, othersWeight, othersWeight, recommendation);
            recommendation.addStockWeight(othersWeightEntity);
        }
    }

    private BigDecimal getWeight(FundModelInputRequest input, AssetCategory category) {
        if (category == null || input == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal weight = switch (category) {
            case STOCK -> input.stockWeight();
            case REPO -> input.repoWeight();
            case FUTURE -> input.futureWeight();
            case FUND -> input.fundWeight();
        };
        return weight != null ? weight : BigDecimal.ZERO;
    }

    private FundModelInputRequest createModelInput(FundResponse fund) {
        Map<AssetCategory, BigDecimal> weights = getFundWeights(fund.code());
        MarketDataRequest marketRow = getLatestMarketData();
        List<FundPeriodMetricResponse> metrics = fundPeriodMetricService.getLatestByFundCode(fund.code());
        BigDecimal fundReturn = resolveDailyReturn(metrics);
        BigDecimal portfolioGrowth = resolvePortfolioGrowth(metrics);
        FundPriceData priceData = fundPriceDataRepository.findFirstByFundCodeOrderByDataDateDesc(fund.code())
                .orElse(null);

        return aiRecommendationMapper.toModelInput(weights, marketRow, fundReturn, portfolioGrowth, priceData);
    }

    private Map<AssetCategory, BigDecimal> getFundWeights(String fundCode) {
        List<FundDistributionResponse> distributions = fundDistributionService.getLatestByFundCode(fundCode);
        return fundDistributionConverter.toWeightsMapFromResponses(distributions);
    }

    private MarketDataRequest getLatestMarketData() {
        return marketDataRepository.findFirstByOrderByDataDateDesc()
                .map(data -> new MarketDataRequest(
                        data.getDataDate(),
                        data.getUsdReturn(),
                        data.getGoldReturn(),
                        data.getBrentReturn(),
                        data.getUs10yReturn(),
                        data.getCdsSpreadBps(),
                        data.getAnnualInflation(),
                        data.getPolicyRate()
                ))
                .orElseGet(() -> {
                    log.warn("MarketData table is empty, falling back to safe default values.");
                    return new MarketDataRequest(
                            LocalDate.now(),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            MarketConstants.DEFAULT_CDS,
                            MarketConstants.DEFAULT_INFLATION,
                            MarketConstants.DEFAULT_POLICY_RATE
                    );
                });
    }

    private BigDecimal resolveDailyReturn(List<FundPeriodMetricResponse> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return BigDecimal.ZERO;
        }
        FundPeriodMetricResponse latest = metrics.getFirst();
        return latest.dailyReturn() != null ? latest.dailyReturn() : BigDecimal.ZERO;
    }

    private BigDecimal resolvePortfolioGrowth(List<FundPeriodMetricResponse> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return BigDecimal.ZERO;
        }
        FundPeriodMetricResponse latest = metrics.getFirst();
        if (latest.totalValue() != null && latest.previousTotalValue() != null && latest.previousTotalValue().compareTo(BigDecimal.ZERO) > 0) {
            return latest.totalValue().subtract(latest.previousTotalValue()).divide(latest.previousTotalValue(), 12, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private float[] predictWeights(FundModelInputRequest input) {
        float[] stateInput = createStateInput(input);
        return onnxModelService.getAction(stateInput);
    }

    private float[] createStateInput(FundModelInputRequest input) {
        return new float[]{
                toFloat(input.usdReturn()),
                toFloat(input.goldReturn()),
                toFloat(input.brentReturn()),
                toFloat(input.us10yReturn()),
                toFloat(input.cdsSpreadBps()) / CDS_NORMALIZATION_FACTOR,
                toFloat(input.annualInflation()) / PERCENT_NORMALIZATION_FACTOR,
                toFloat(input.policyRate()) / PERCENT_NORMALIZATION_FACTOR,
                toFloat(input.fundReturn()),
                toFloat(input.portfolioGrowth()),
                toFloat(input.activeValue()) / BILLION_SCALE,
                toFloat(input.cashValue()) / MILLION_SCALE,
                toFloat(input.investorCount()) / INVESTOR_COUNT_SCALE,
                toFloat(input.stockWeight()),
                toFloat(input.repoWeight()),
                toFloat(input.futureWeight()),
                toFloat(input.fundWeight())
        };
    }

    private static float toFloat(BigDecimal value) {
        return value != null ? value.floatValue() : 0.0f;
    }

    @Override
    @Transactional
    @CacheEvict(cacheManager = "caffeineCacheManager", cacheNames = CacheNames.PERFORMANCE_COMPARISON, allEntries = true)
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
                    recommendation, recommendation.getFund().getCode(), 30,
                    recommendation.getSimulationWeights(), recommendation.getSimulationStockWeights());
        }

        aiRecommendationRepository.save(recommendation);
    }

    private void addWeightToEntity(AiRecommendation aiRecommendation, AssetCategory assetCategory, BigDecimal recommended, BigDecimal current) {
        AiRecommendationWeight weight = aiRecommendationMapper.toWeightEntity(assetCategory, recommended, current);
        aiRecommendation.addWeight(weight);
    }
}
