package com.akademi.finsight.stresstest.mapper;

import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.PortfolioResultDto;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.entity.StressTestResultDetail;
import com.akademi.finsight.stresstest.enums.PortfolioType;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.user.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StressTestMapper {
    public StressTestResult createStressTestResult(User user, Fund fund, SimulationType simulationType) {
        return StressTestResult.builder()
                .user(user)
                .fund(fund)
                .simulationType(simulationType)
                .build();
    }

    public StressTestResultDetail createDetail(PortfolioType type, BigDecimal initialVal, ModelInferenceResult result) {
        return StressTestResultDetail.builder()
                .portfolioType(type)
                .initialValue(initialVal)
                .expectedImpactRate(result.expectedImpactRate())
                .postShockValue(result.postShockValue())
                .build();
    }

    public StressTestInferenceResponseDto toInferenceResponseDto(StressTestResult result) {
        PortfolioResultDto currentPortfolio = null;
        PortfolioResultDto simulationPortfolio = null;
        PortfolioResultDto benchmarkPortfolio = null;

        if (result.getDetails() != null) {
            for (StressTestResultDetail detail : result.getDetails()) {
                PortfolioResultDto dto = PortfolioResultDto.builder()
                        .initialValue(detail.getInitialValue())
                        .expectedImpactRate(detail.getExpectedImpactRate())
                        .postShockValue(detail.getPostShockValue())
                        .build();

                if (detail.getPortfolioType() == PortfolioType.CURRENT_PORTFOLIO) {
                    currentPortfolio = dto;
                } else if (detail.getPortfolioType() == PortfolioType.SIMULATION_PORTFOLIO) {
                    simulationPortfolio = dto;
                } else if (detail.getPortfolioType() == PortfolioType.BENCHMARK) {
                    benchmarkPortfolio = dto;
                }
            }
        }

        return StressTestInferenceResponseDto.builder()
                .id(result.getId())
                .scenarioKey(result.getSimulationType() != null ? result.getSimulationType().name() : "EQUITY_SHOCK")
                .currentPortfolioResult(currentPortfolio)
                .simulationPortfolioResult(simulationPortfolio)
                .benchmarkPortfolioResult(benchmarkPortfolio)
                .llmComment("")
                .build();
    }
}