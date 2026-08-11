package com.akademi.finsight.stresstest;

import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.engine.impl.RuleBasedStressTestEngineImpl;
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RuleBasedStressTestEngineImplTest {

    private final RuleBasedStressTestEngineImpl engine = new RuleBasedStressTestEngineImpl();

    @Test
    void getStrategyType_isManualRule() {
        assertThat(engine.getStrategyType()).isEqualTo(ExecutionStrategyType.MANUAL_RULE);
    }

    @Test
    void equityShock_appliesEquityBondFxImpactWeights() {
        PortfolioDataDto portfolio = PortfolioDataDto.builder()
                .initialValue(BigDecimal.valueOf(1000))
                .assetWeights(Map.of("EQUITY", 0.5f, "BOND", 0.3f, "FX", 0.2f))
                .build();

        ModelInferenceResult result = engine.runInference("EQUITY_SHOCK", portfolio);

        // (0.5*-0.10) + (0.3*-0.005) + (0.2*0.02) = -0.05 - 0.0015 + 0.004 = -0.0475
        BigDecimal expectedRate = BigDecimal.valueOf(-0.0475).setScale(4, RoundingMode.HALF_UP);
        assertThat(result.expectedImpactRate()).isEqualByComparingTo(expectedRate);

        BigDecimal expectedValue = BigDecimal.valueOf(1000)
                .multiply(BigDecimal.ONE.add(expectedRate))
                .setScale(2, RoundingMode.HALF_UP);
        assertThat(result.postShockValue()).isEqualByComparingTo(expectedValue);
    }

    @Test
    void equityShock_isCaseInsensitive() {
        PortfolioDataDto portfolio = PortfolioDataDto.builder()
                .initialValue(BigDecimal.valueOf(1000))
                .assetWeights(Map.of("equity", 1.0f))
                .build();

        ModelInferenceResult result = engine.runInference("equity_shock", portfolio);

        assertThat(result.expectedImpactRate()).isEqualByComparingTo(BigDecimal.valueOf(-0.10).setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    void interestRateShock_appliesEquityBondFxCashImpactWeights() {
        PortfolioDataDto portfolio = PortfolioDataDto.builder()
                .initialValue(BigDecimal.valueOf(1000))
                .assetWeights(Map.of("EQUITY", 0.25f, "BOND", 0.25f, "FX", 0.25f, "CASH", 0.25f))
                .build();

        ModelInferenceResult result = engine.runInference("INTEREST_RATE_SHOCK", portfolio);

        // (0.25*-0.03)+(0.25*-0.08)+(0.25*0.01)+(0.25*0.005) = -0.0075-0.02+0.0025+0.00125 = -0.02375
        BigDecimal expectedRate = BigDecimal.valueOf(-0.02375f).setScale(4, RoundingMode.HALF_UP);
        assertThat(result.expectedImpactRate()).isEqualByComparingTo(expectedRate);
    }

    @Test
    void unknownScenario_appliesDefaultMinusFivePercent() {
        PortfolioDataDto portfolio = PortfolioDataDto.builder()
                .initialValue(BigDecimal.valueOf(1000))
                .assetWeights(Map.of("EQUITY", 1.0f))
                .build();

        ModelInferenceResult result = engine.runInference("UNKNOWN_SCENARIO", portfolio);

        assertThat(result.expectedImpactRate()).isEqualByComparingTo(BigDecimal.valueOf(-0.05).setScale(4, RoundingMode.HALF_UP));
        assertThat(result.postShockValue()).isEqualByComparingTo(BigDecimal.valueOf(950.00));
    }

    @Test
    void emptyWeights_zeroImpact() {
        PortfolioDataDto portfolio = PortfolioDataDto.builder()
                .initialValue(BigDecimal.valueOf(1000))
                .assetWeights(Map.of())
                .build();

        ModelInferenceResult result = engine.runInference("EQUITY_SHOCK", portfolio);

        assertThat(result.expectedImpactRate()).isEqualByComparingTo(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        assertThat(result.postShockValue()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    void missingAssetKeys_treatedAsZeroWeight() {
        PortfolioDataDto portfolio = PortfolioDataDto.builder()
                .initialValue(BigDecimal.valueOf(1000))
                .assetWeights(Map.of("EQUITY", 1.0f)) // BOND/FX absent
                .build();

        ModelInferenceResult result = engine.runInference("EQUITY_SHOCK", portfolio);

        assertThat(result.expectedImpactRate()).isEqualByComparingTo(BigDecimal.valueOf(-0.10).setScale(4, RoundingMode.HALF_UP));
    }
}
