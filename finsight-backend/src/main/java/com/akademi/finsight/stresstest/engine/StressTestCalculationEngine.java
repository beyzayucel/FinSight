package com.akademi.finsight.stresstest.engine;

import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.request.StressTestInferenceRequestDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;

public interface StressTestCalculationEngine {
    ModelInferenceResult runInference(String scenarioKey, PortfolioDataDto portfolioData);
}
