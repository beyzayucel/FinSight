package com.akademi.finsight.fund.mapper;

import com.akademi.finsight.fund.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.dto.response.AiWeightResponse;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.entity.AiRecommendation;
import com.akademi.finsight.fund.entity.AiRecommendationWeight;
import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.entity.RecommendationStatus;
import com.akademi.finsight.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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

    public AIRecommendationResponse toResponse(AiRecommendation entity) {
        if (entity == null) {
            return null;
        }

        Map<AssetCategory, AiWeightResponse> weightsMap = new HashMap<>();

        if (entity.getWeights() != null) {
            for (Map.Entry<AssetCategory, AiRecommendationWeight> entry : entity.getWeights().entrySet()) {
                AiRecommendationWeight weightEntity = entry.getValue();

                AiWeightResponse weightDto = new AiWeightResponse(weightEntity.getRecommendedWeight(), weightEntity.getCurrentWeight());

                weightsMap.put(entry.getKey(), weightDto);
            }
        }

        return AIRecommendationResponse.builder()
                .id(entity.getId())
                .fundId(entity.getFund().getId())
                .status(entity.getStatus())
                .rationale(entity.getRationale())
                .expectedRiskChange(entity.getExpectedRiskChange())
                .note(entity.getNote())
                .weights(weightsMap)
                .build();
    }

}