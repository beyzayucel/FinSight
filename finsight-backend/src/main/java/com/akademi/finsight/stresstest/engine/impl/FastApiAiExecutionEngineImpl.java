package com.akademi.finsight.stresstest.engine.impl;

import com.akademi.finsight.stresstest.client.FastApiClient;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.engine.StressTestCalculationEngine;
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FastApiAiExecutionEngineImpl implements StressTestCalculationEngine {

    private final FastApiClient fastApiClient;

    @Override
    public ModelInferenceResult runInference(String scenarioKey, PortfolioDataDto portfolioData) {
        return fastApiClient.predict(scenarioKey, portfolioData);
    }

    @Override
    public ExecutionStrategyType getStrategyType() {
        return ExecutionStrategyType.FASTAPI_AI;
    }
}
