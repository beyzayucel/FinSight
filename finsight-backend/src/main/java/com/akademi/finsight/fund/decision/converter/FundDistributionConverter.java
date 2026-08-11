package com.akademi.finsight.fund.decision.converter;

import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.decision.entity.AssetCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FundDistributionConverter {

    private final AssetCategoryConverter categoryConverter = new AssetCategoryConverter();

    /**
     * Dağılım listesini yüzdelik formatta Map'e dönüştürür (STOCK -> 94.90)
     */
    public Map<AssetCategory, BigDecimal> toPercentageWeightsMap(List<FundDistributionResponse> distributions) {
        Map<AssetCategory, BigDecimal> weightsMap = zeroFilledWeightsMap();
        for (FundDistributionResponse dist : distributions) {
            AssetCategory category = categoryConverter.convertToEntityAttribute(dist.category());
            if (Objects.nonNull(category)) {
                weightsMap.put(category, dist.weight());
            }
        }
        return weightsMap;
    }

    public Map<AssetCategory, BigDecimal> toWeightsMapFromResponses(List<FundDistributionResponse> distributions) {
        Map<AssetCategory, BigDecimal> weightsMap = zeroFilledWeightsMap();
        for (FundDistributionResponse dist : distributions) {
            AssetCategory category = categoryConverter.convertToEntityAttribute(dist.category());
            if (Objects.nonNull(category)) {
                weightsMap.put(category, dist.weight().divide(BigDecimal.valueOf(100)));
            }
        }
        return weightsMap;
    }

    private Map<AssetCategory, BigDecimal> zeroFilledWeightsMap() {
        Map<AssetCategory, BigDecimal> weightsMap = new HashMap<>();
        for (AssetCategory category : AssetCategory.values()) {
            weightsMap.put(category, BigDecimal.ZERO);
        }
        return weightsMap;
    }

}
