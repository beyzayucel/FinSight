package com.akademi.finsight.stresstest;

import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.entity.StressTestResultDetail;
import com.akademi.finsight.stresstest.enums.PortfolioType;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.mapper.StressTestMapper;
import com.akademi.finsight.user.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

class StressTestMapperTest {

    private final StressTestMapper mapper = new StressTestMapper();

    @Test
    void createStressTestResult_setsUserFundAndSimulationType() {
        User user = mock(User.class);
        Fund fund = mock(Fund.class);

        StressTestResult result = mapper.createStressTestResult(user, fund, SimulationType.EQUITY_SHOCK);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getFund()).isEqualTo(fund);
        assertThat(result.getSimulationType()).isEqualTo(SimulationType.EQUITY_SHOCK);
    }

    @Test
    void createDetail_mapsAllFieldsFromInferenceResult() {
        ModelInferenceResult inferenceResult = new ModelInferenceResult(BigDecimal.valueOf(-0.05), BigDecimal.valueOf(950));

        StressTestResultDetail detail = mapper.createDetail(PortfolioType.CURRENT_PORTFOLIO, BigDecimal.valueOf(1000), inferenceResult);

        assertThat(detail.getPortfolioType()).isEqualTo(PortfolioType.CURRENT_PORTFOLIO);
        assertThat(detail.getInitialValue()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(detail.getExpectedImpactRate()).isEqualByComparingTo(BigDecimal.valueOf(-0.05));
        assertThat(detail.getPostShockValue()).isEqualByComparingTo(BigDecimal.valueOf(950));
    }

    @Test
    void toInferenceResponseDto_mapsAllThreePortfolioTypesToCorrectFields() {
        StressTestResult result = StressTestResult.builder()
                .simulationType(SimulationType.EQUITY_SHOCK)
                .build();
        result.addDetail(mapper.createDetail(PortfolioType.CURRENT_PORTFOLIO, BigDecimal.valueOf(1000),
                new ModelInferenceResult(BigDecimal.valueOf(-0.05), BigDecimal.valueOf(950))));
        result.addDetail(mapper.createDetail(PortfolioType.SIMULATION_PORTFOLIO, BigDecimal.valueOf(900),
                new ModelInferenceResult(BigDecimal.valueOf(-0.04), BigDecimal.valueOf(864))));
        result.addDetail(mapper.createDetail(PortfolioType.BENCHMARK, BigDecimal.valueOf(950),
                new ModelInferenceResult(BigDecimal.valueOf(-0.03), BigDecimal.valueOf(921.5))));

        StressTestInferenceResponseDto response = mapper.toInferenceResponseDto(result);

        assertThat(response.scenarioKey()).isEqualTo("EQUITY_SHOCK");
        assertThat(response.currentPortfolioResult().initialValue()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(response.simulationPortfolioResult().initialValue()).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(response.benchmarkPortfolioResult().initialValue()).isEqualByComparingTo(BigDecimal.valueOf(950));
        assertThat(response.llmComment()).isEmpty();
    }

    @Test
    void toInferenceResponseDto_nullSimulationType_defaultsScenarioKeyToEquityShock() {
        StressTestResult result = StressTestResult.builder().build();

        StressTestInferenceResponseDto response = mapper.toInferenceResponseDto(result);

        assertThat(response.scenarioKey()).isEqualTo("EQUITY_SHOCK");
    }

    @Test
    void toInferenceResponseDto_nullDetails_allPortfolioResultsAreNull() {
        StressTestResult result = StressTestResult.builder()
                .simulationType(SimulationType.INTEREST_RATE_SHOCK)
                .build();
        result.setDetails(null);

        StressTestInferenceResponseDto response = mapper.toInferenceResponseDto(result);

        assertThat(response.currentPortfolioResult()).isNull();
        assertThat(response.simulationPortfolioResult()).isNull();
        assertThat(response.benchmarkPortfolioResult()).isNull();
    }
}