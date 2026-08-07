package com.akademi.finsight.stresstest.engine;

import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.enums.SimulationType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static com.akademi.finsight.stresstest.enums.AssetType.*;

@Component("ruleBasedStressTestEngine")
public class RuleBasedStressTestEngineImpl implements StressTestCalculationEngine {

    @Override
    public ModelInferenceResult runInference(String scenarioKey, PortfolioDataDto portfolioData) {
        SimulationType simulationType = SimulationType.valueOf(scenarioKey);
        Map<String, Float> weights = portfolioData.assetWeights();

        float equityWeight = getWeight(weights, EQUITY.name());
        float bondWeight = getWeight(weights, BOND.name());
        float fxWeight = getWeight(weights, FX.name());
        float cashWeight = getWeight(weights, CASH.name());

        // Senaryoya göre etki oranının (Impact Rate %) kural tabanlı hesaplanması
        double impactRatePercent;
        if (simulationType == SimulationType.EQUITY_SHOCK) {
            impactRatePercent = (equityWeight * -10.0) + (bondWeight * -1.0) + (fxWeight * 1.0) + (cashWeight * 0.0);
        } else { // INTEREST_STRESS veya diğer senaryolar
            impactRatePercent = (equityWeight * -3.0) + (bondWeight * -5.0) + (fxWeight * -1.0) + (cashWeight * 1.5);
        }

        BigDecimal expectedImpactRate = BigDecimal.valueOf(impactRatePercent).setScale(2, RoundingMode.HALF_UP);

        BigDecimal initialValue = portfolioData.initialValue();
        BigDecimal impactMultiplier = BigDecimal.valueOf(1 + (impactRatePercent / 100.0));
        BigDecimal postShockValue = initialValue.multiply(impactMultiplier).setScale(2, RoundingMode.HALF_UP);

        return new ModelInferenceResult(expectedImpactRate, postShockValue);
    }

    private float getWeight(Map<String, Float> weights, String assetKey) {
        if (weights == null) return 0.0f;

        for (Map.Entry<String, Float> entry : weights.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(assetKey)) {
                return entry.getValue() != null ? entry.getValue() : 0.0f;
            }
        }
        return 0.0f;
    }
}