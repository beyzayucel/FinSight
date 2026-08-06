package com.akademi.finsight.stresstest.service;

import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;

import java.math.BigDecimal;

public interface OnnxModelRunner {
    ModelInferenceResult runInference(String scenarioKey, PortfolioDataDto portfolioData);
}
