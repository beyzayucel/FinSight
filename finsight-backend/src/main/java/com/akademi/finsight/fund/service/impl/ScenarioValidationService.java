package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Service
public class ScenarioValidationService {

    private static final BigDecimal MIN_TOTAL_WEIGHT = new BigDecimal("99.99");
    private static final BigDecimal MAX_TOTAL_WEIGHT = new BigDecimal("100.01");
    private static final BigDecimal MAX_ALLOWED_DEVIATION = new BigDecimal("10.00");
    private static final BigDecimal MIN_STOCK_WEIGHT_PERCENT = new BigDecimal("80.00");

    public void validate(Map<AssetCategory, BigDecimal> targetWeights, Map<AssetCategory, BigDecimal> currentWeights) {
        if (Objects.isNull(targetWeights) || targetWeights.isEmpty()) {
            throw new FundValidationException(FundErrorType.INVALID_SCENARIO_TOTAL);
        }

        validateTotalWeight(targetWeights);
        validateDeviation(targetWeights, currentWeights);
        validateMinimumStockWeight(targetWeights);
    }

    private void validateTotalWeight(Map<AssetCategory, BigDecimal> weights) {
        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal weight : weights.values()) {
            if (Objects.isNull(weight)) {
                throw new FundValidationException(FundErrorType.INVALID_INPUT);
            }
            total = total.add(weight);
        }

        if (total.compareTo(MIN_TOTAL_WEIGHT) < 0 || total.compareTo(MAX_TOTAL_WEIGHT) > 0) {
            throw new FundValidationException(FundErrorType.INVALID_SCENARIO_TOTAL);
        }
    }

    private void validateDeviation(Map<AssetCategory, BigDecimal> targetWeights, Map<AssetCategory, BigDecimal> currentWeights) {
        for (Map.Entry<AssetCategory, BigDecimal> entry : targetWeights.entrySet()) {
            AssetCategory category = entry.getKey();
            BigDecimal targetWeight = entry.getValue();

            if (Objects.isNull(targetWeight)) {
                throw new FundValidationException(FundErrorType.INVALID_INPUT);
            }

            BigDecimal currentWeight = currentWeights.getOrDefault(category, BigDecimal.ZERO);

            BigDecimal deviation = targetWeight.subtract(currentWeight).abs();

            if (deviation.compareTo(MAX_ALLOWED_DEVIATION) > 0) {
                throw new FundValidationException(FundErrorType.INVALID_SCENARIO_DEVIATION, category, deviation);
            }
        }
    }

    private void validateMinimumStockWeight(Map<AssetCategory, BigDecimal> weights) {
        BigDecimal stockWeight = weights.get(AssetCategory.STOCK);

        if (Objects.nonNull(stockWeight) && stockWeight.compareTo(MIN_STOCK_WEIGHT_PERCENT) < 0) {
            throw new FundValidationException(FundErrorType.INVALID_SCENARIO_STOCK_FLOOR);
        }

    }

}