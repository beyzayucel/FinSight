package com.akademi.finsight.stresstest.service.impl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.akademi.finsight.stresstest.config.OnnxModelRegistry;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import com.akademi.finsight.stresstest.service.OnnxModelRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnnxModelRunnerImpl implements OnnxModelRunner {
    private final OnnxModelRegistry onnxModelRegistry;

    @Override
    public ModelInferenceResult runInference(String scenarioKey, PortfolioDataDto portfolioData) {

        OrtSession session = onnxModelRegistry.getSession(scenarioKey);
        float[] inputFeatures = extractFeatures(portfolioData);

        float[][] inputMatrix = new float[][]{ inputFeatures };

         try {
            try(OnnxTensor inputTensor = OnnxTensor.createTensor(onnxModelRegistry.getEnv(), inputMatrix)){

                String inputName = session.getInputNames().iterator().next();
                Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);

                try(OrtSession.Result result = session.run(inputs)){
                    float impactRateFloat = parseOutputValue(result.get(0).getValue());
                    float postShockFloat = parseOutputValue(result.get(1).getValue());

                    BigDecimal expectedImpactRate = new BigDecimal(Float.toString(impactRateFloat));
                    BigDecimal postShockValue = new BigDecimal(Float.toString(postShockFloat));

                    return new ModelInferenceResult(expectedImpactRate, postShockValue);
                    
                }

            }
        } catch (OrtException e) {
            log.error("ONNX Inference error [Scenario: {}]: {}", scenarioKey, e.getMessage(), e);
            throw new StressTestException(StressTestErrorType.MODEL_INFERENCE_ERROR, e);
        }
    }

    private float parseOutputValue(Object outputValue) {
        if (outputValue instanceof float[][]) {
            return ((float[][]) outputValue)[0][0];
        } else if (outputValue instanceof float[]) {
            return ((float[]) outputValue)[0];
        } else {
            throw new StressTestException(StressTestErrorType.MODEL_INFERENCE_ERROR);
        }
    }

    private float[] extractFeatures(PortfolioDataDto portfolioData) {
        Map<String, Float> weights = portfolioData.assetWeights();

        return new float[] {
                getWeight(weights, "EQUITY"),
                getWeight(weights, "BOND"),
                getWeight(weights, "FX"),
                getWeight(weights, "CASH")
        };
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
