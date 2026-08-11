package com.akademi.finsight.stresstest.engine.impl;

import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.engine.AssetWeights;
import com.akademi.finsight.stresstest.engine.StressTestCalculationEngine;
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component("ruleBasedStressTestEngine")
public class RuleBasedStressTestEngineImpl implements StressTestCalculationEngine {

    private static final String SCENARIO_EQUITY_SHOCK = "EQUITY_SHOCK";
    private static final String SCENARIO_INTEREST_RATE_SHOCK = "INTEREST_RATE_SHOCK";

    @Override
    public ModelInferenceResult runInference(String scenarioKey, PortfolioDataDto portfolioData) {
        Map<String, Float> weights = portfolioData.assetWeights();
        BigDecimal initialValue = portfolioData.initialValue();

        float totalImpactRateFloat = calculateManualImpact(scenarioKey, weights);

        BigDecimal expectedImpactRate = new BigDecimal(Float.toString(totalImpactRateFloat))
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal multiplier = BigDecimal.ONE.add(expectedImpactRate);
        BigDecimal postShockValue = initialValue.multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        return new ModelInferenceResult(expectedImpactRate, postShockValue);
    }

    @Override
    public ExecutionStrategyType getStrategyType() {
        return ExecutionStrategyType.MANUAL_RULE;
    }

    private float calculateManualImpact(String scenarioKey, Map<String, Float> weights) {
        if (weights == null || weights.isEmpty()) {
            return 0.0f;
        }

        AssetWeights parsedWeights = parseAssetWeights(weights);

        if (SCENARIO_EQUITY_SHOCK.equalsIgnoreCase(scenarioKey)) {
            return calculateEquityShockImpact(parsedWeights);
        } else if (SCENARIO_INTEREST_RATE_SHOCK.equalsIgnoreCase(scenarioKey)) {
            return calculateInterestRateShockImpact(parsedWeights);
        }

        return -0.05f;
    }

    private AssetWeights parseAssetWeights(Map<String, Float> weights) {
        float equity = getWeight(weights, "EQUITY");
        float bond   = getWeight(weights, "BOND");
        float fx     = getWeight(weights, "FX");
        float cash   = getWeight(weights, "CASH");

        return new AssetWeights(equity, bond, fx, cash);
    }

    private float calculateEquityShockImpact(AssetWeights weights){
        return (weights.equity() * -0.10f) + (weights.bond() * -0.005f) + (weights.fx() * 0.02f);
    }

    private float calculateInterestRateShockImpact(AssetWeights weights) {
        return (weights.equity() * -0.03f) + (weights.bond() * -0.08f) + (weights.fx() * 0.01f) + (weights.cash() * 0.005f);
    }

    private float getWeight(Map<String, Float> weights, String key) {
        return weights.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(0.0f);
    }
}