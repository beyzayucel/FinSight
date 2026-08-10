package com.akademi.finsight.fund.decision.mapper;

import com.akademi.finsight.ai.model.dto.request.MarketDataRequest;
import com.akademi.finsight.ai.model.dto.request.FundModelInputRequest;
import com.akademi.finsight.fund.decision.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.decision.dto.response.AiWeightResponse;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.AiRecommendationWeight;
import com.akademi.finsight.fund.decision.entity.AssetCategory;
import com.akademi.finsight.ai.model.entity.FundPriceData;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import com.akademi.finsight.fund.mapper.FundMapper;
import com.akademi.finsight.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.akademi.finsight.fund.decision.dto.response.AIRecommendationStockWeightResponse;
import com.akademi.finsight.fund.decision.entity.AiRecommendationStockWeight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiRecommendationMapper {

    private final FundMapper fundMapper;
    private final MessageSource messageSource;

    public AiRecommendation toEntity(FundResponse fund, User user) {
        String rationale = messageSource.getMessage("fund.recommendation.rationale", null, LocaleContextHolder.getLocale());
        String expectedRiskChange = messageSource.getMessage("fund.recommendation.expected_risk_change", null, LocaleContextHolder.getLocale());

        return AiRecommendation.builder()
                .user(user)
                .fund(fundMapper.toEntity(fund))
                .status(RecommendationStatus.PENDING)
                .rationale(rationale)
                .expectedRiskChange(expectedRiskChange)
                .build();
    }

    public FundModelInputRequest toModelInput(
            Map<AssetCategory, BigDecimal> weights,
            MarketDataRequest marketRow,
            BigDecimal fundReturn,
            BigDecimal portfolioGrowth,
            FundPriceData priceData) {

        return FundModelInputRequest.builder()
                .stockWeight(weights.getOrDefault(AssetCategory.STOCK, BigDecimal.ZERO))
                .repoWeight(weights.getOrDefault(AssetCategory.REPO, BigDecimal.ZERO))
                .futureWeight(weights.getOrDefault(AssetCategory.FUTURE, BigDecimal.ZERO))
                .fundWeight(weights.getOrDefault(AssetCategory.FUND, BigDecimal.ZERO))
                .usdReturn(marketRow.usdReturn())
                .goldReturn(marketRow.goldReturn())
                .brentReturn(marketRow.brentReturn())
                .us10yReturn(marketRow.us10yReturn())
                .cdsSpreadBps(marketRow.cdsSpreadBps())
                .annualInflation(marketRow.annualInflation())
                .policyRate(marketRow.policyRate())
                .fundReturn(fundReturn)
                .portfolioGrowth(portfolioGrowth)
                .activeValue(priceData != null ? valueOrZero(priceData.getActiveValue()) : BigDecimal.ZERO)
                .cashValue(priceData != null ? valueOrZero(priceData.getCashValue()) : BigDecimal.ZERO)
                .investorCount(priceData != null ? valueOrZero(priceData.getInvestorCount()) : BigDecimal.ZERO)
                .build();
    }

    public AiRecommendationWeight toWeightEntity(AssetCategory category, BigDecimal recommended, BigDecimal current) {
        return AiRecommendationWeight.builder()
                .category(category)
                .recommendedWeight(recommended.setScale(2, RoundingMode.HALF_UP))
                .currentWeight(current.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    public AiRecommendationStockWeight toStockWeightEntity(String assetCode, BigDecimal recommended, BigDecimal current, AiRecommendation recommendation) {
        return AiRecommendationStockWeight.builder()
                .assetCode(assetCode)
                .recommendedWeight(recommended.setScale(2, RoundingMode.HALF_UP))
                .currentWeight(current.setScale(2, RoundingMode.HALF_UP))
                .recommendation(recommendation)
                .build();
    }

    public AIRecommendationResponse toResponse(AiRecommendation entity) {
        if (entity == null) {
            return null;
        }

        Map<AssetCategory, AiWeightResponse> weightsMap = new HashMap<>();

        if (entity.getWeights() != null) {
            for (Map.Entry<AssetCategory, AiRecommendationWeight> entry : entity.getWeights().entrySet()) {
                AiRecommendationWeight weightEntity = entry.getValue();

                AiWeightResponse weightDto = new AiWeightResponse(
                        weightEntity.getRecommendedWeight(),
                        weightEntity.getCurrentWeight());

                weightsMap.put(entry.getKey(), weightDto);
            }
        }

        List<AIRecommendationStockWeightResponse> stockWeightsList = new ArrayList<>();
        if (entity.getStockWeights() != null) {
            for (AiRecommendationStockWeight sw : entity.getStockWeights().values()) {
                stockWeightsList.add(new AIRecommendationStockWeightResponse(
                        sw.getAssetCode(),
                        sw.getRecommendedWeight(),
                        sw.getCurrentWeight()
                ));
            }
            stockWeightsList.sort((a, b) -> {
                if ("Others".equals(a.assetCode()) || "+ Diğer".equals(a.assetCode())) return 1;
                if ("Others".equals(b.assetCode()) || "+ Diğer".equals(b.assetCode())) return -1;
                return b.currentWeight().compareTo(a.currentWeight());
            });
        }

        return AIRecommendationResponse.builder()
                .id(entity.getId())
                .fundId(entity.getFund().getId())
                .status(entity.getStatus())
                .rationale(entity.getRationale())
                .expectedRiskChange(entity.getExpectedRiskChange())
                .note(entity.getNote())
                .weights(weightsMap)
                .stockWeights(stockWeightsList)
                .build();
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
    }
}