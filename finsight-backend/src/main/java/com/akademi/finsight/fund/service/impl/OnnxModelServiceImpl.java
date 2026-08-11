package com.akademi.finsight.fund.service.impl;

import ai.onnxruntime.*;
import com.akademi.finsight.fund.service.OnnxModelService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;

@Slf4j
@Service
public class OnnxModelServiceImpl implements OnnxModelService, AutoCloseable {

    private static final String MODEL_PATH = "model/dqn_portfolio_model.onnx";

    private OrtEnvironment env;
    private OrtSession session;

    //bu class entegrasyon sonrası düzenlenecek

    @PostConstruct
    public void init() {
        try {
            log.info("Loading ONNX portfolio DQN model...");
            env = OrtEnvironment.getEnvironment();

            ClassPathResource resource = new ClassPathResource(MODEL_PATH);

            try (InputStream is = resource.getInputStream()) {
                byte[] modelBytes = is.readAllBytes();
                session = env.createSession(modelBytes, new OrtSession.SessionOptions());
            }
            log.info("ONNX portfolio DQN model loaded successfully.");
        } catch (Exception e) {
            log.error("Failed to load ONNX model session!", e);
            session = null;
        }
    }

    @Override
    public float[] getAction(float[] stateInput) {
        if (stateInput == null || stateInput.length == 0) {
            throw new IllegalArgumentException("State input cannot be null or empty.");
        }

        if (session == null) {
            log.warn("ONNX model session is not available. Falling back to current portfolio weights heuristic.");
            return calculateHeuristicFallback(stateInput);
        }

        try {
            log.info("Running ONNX model inference. Input size: {}, array: {}", stateInput.length, java.util.Arrays.toString(stateInput));

            float[][] inputData = new float[1][stateInput.length];
            System.arraycopy(stateInput, 0, inputData[0], 0, stateInput.length);

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData)) {
                String inputName = session.getInputNames().iterator().next();

                try (OrtSession.Result results = session.run(Collections.singletonMap(inputName, inputTensor))) {
                    float[][] outputData = (float[][]) results.get(0).getValue();
                    float[] result = outputData[0];
                    log.info("ONNX model inference output weights: {}", java.util.Arrays.toString(result));
                    return result;
                }
            }
        } catch (OrtException e) {
            log.error("Error running ONNX model inference, falling back to heuristic weights", e);
            return calculateHeuristicFallback(stateInput);
        }
    }

    private float[] calculateHeuristicFallback(float[] stateInput) {
        int len = stateInput.length;
        if (len < 4) {
            return new float[] { 0.915f, 0.055f, 0.02f, 0.01f };
        }

        float currentStock = stateInput[len - 4];
        float currentRepo = stateInput[len - 3];
        float currentFuture = stateInput[len - 2];
        float currentFund = stateInput[len - 1];

        if (currentStock <= 0.01f) {
            return new float[] { 0.915f, 0.055f, 0.02f, 0.01f };
        }

        float reduction = Math.min(0.035f, currentStock * 0.037f);
        float targetStock = Math.max(0.80f, currentStock - reduction);
        float diff = currentStock - targetStock;

        float targetRepo = currentRepo + (diff * 0.65f);
        float targetFuture = currentFuture + (diff * 0.25f);
        float targetFund = currentFund + (diff * 0.10f);

        float total = targetStock + targetRepo + targetFuture + targetFund;
        if (total <= 0.001f) {
            return new float[] { 0.915f, 0.055f, 0.02f, 0.01f };
        }

        return new float[] {
                targetStock / total,
                targetRepo / total,
                targetFuture / total,
                targetFund / total
        };
    }

    @Override
    @PreDestroy
    public void close() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
        } catch (OrtException e) {
            log.error("Error closing ONNX runtime resources", e);
        }
    }
}
