package com.akademi.finsight.stresstest.engine;

import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import org.springframework.stereotype.Component;


@Component
public interface StressTestCalculationEngine {
    ModelInferenceResult runInference(String scenarioKey, PortfolioDataDto portfolioData);
    ExecutionStrategyType getStrategyType();
}
