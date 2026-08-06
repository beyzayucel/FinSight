package com.akademi.finsight.stresstest.config;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.*;

import static com.akademi.finsight.stresstest.constant.OnnxModelConstants.*;


@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class OnnxModelRegistry {

    private OrtEnvironment env;
    private OrtSession.SessionOptions sessionOptions;
    private final Map<String,OrtSession> sessions = new ConcurrentHashMap<>();
    private final Map<String,Boolean> modelAvailability = new ConcurrentHashMap<>();

    private final OnnxProperties onnxProperties;

    private final ExecutorService modelLoaderExecutor = Executors.newFixedThreadPool(2);


    // @PostConstruct // model çıktığı zaman kullanılacak
    public void init(){
        this.env = OrtEnvironment.getEnvironment();
        this.sessionOptions = buildSessionOptions();

        CompletableFuture<Void> interestTask = CompletableFuture.runAsync(() -> safeLoad(INTEREST_STRESS), modelLoaderExecutor);
        CompletableFuture<Void> shareTask = CompletableFuture.runAsync(() -> safeLoad(SHARE_STRESS), modelLoaderExecutor);

        CompletableFuture.allOf(interestTask, shareTask)
                .exceptionally(ex -> {
                    log.warn("Some ONNX models failed to load on startup. System will run in degraded mode.");
                    return null;
                })
                .join();

        log.info("Model initialization completed. Availability status: {}", modelAvailability);

    }


    private OrtSession.SessionOptions buildSessionOptions() {
        try {
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();

            options.setIntraOpNumThreads(onnxProperties.getIntraOpNumThreads());
            options.setInterOpNumThreads(onnxProperties.getInterOpNumThreads());

            options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

            return options;

        }catch (OrtException e){
            throw new StressTestException(StressTestErrorType.MODEL_INITIALIZATION_ERROR, e);
        }
    }

    private void safeLoad(String key) {
        try {
            loadModel(key);
            modelAvailability.put(key, true);
            log.info("ONNX Model [{}] loaded successfully.", key);

        } catch (OrtException e) {
            modelAvailability.put(key, false);
            log.error("ONNX Engine Error: Failed to initialize C++ session for model [{}]. Reason: {}",
                    key, e.getMessage(), e);

        } catch (IllegalArgumentException e) {
            modelAvailability.put(key, false);
            log.warn("ONNX Model [{}] file is missing or path is invalid. Feature disabled. Reason: {}",
                    key, e.getMessage());

        } catch (StressTestException e) { //
            modelAvailability.put(key, false);
            log.error("Domain Error during loading ONNX model [{}]: Code: {}, Message: {}",
                    key, e.getCode(), e.getMessage());

        } catch (Exception e) {
            modelAvailability.put(key, false);
            log.error("Unexpected error occurred while loading ONNX model [{}]: {}", key, e.getMessage(), e);
        }
    }

    private void loadModel(String key) throws Exception {
        String resourcePath = MODEL_PATHS.get(key);

        if (resourcePath == null){
            log.error("CRITICAL: Invalid model key specified. Key [{}] is not defined in MODEL_PATHS mapping.", key);
            throw new StressTestException(StressTestErrorType.INVALID_MODEL_KEY);
        }
        loadModelFromResource(key, resourcePath);
    }

    private void loadModelFromResource(String key, String resourcePath) throws Exception{
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {

            if (is == null) {
                log.error("CRITICAL: Model resource file is missing at path: {}", resourcePath);
                throw new StressTestException(StressTestErrorType.MODEL_NOT_FOUND);
            }

            byte[] modelBytes = is.readAllBytes();
            OrtSession session = env.createSession(modelBytes, sessionOptions);
            logModelSignature(key, session);
            sessions.put(key, session);
        }
    }

    private void logModelSignature(String key, OrtSession session) {
        try {
            Map<String, NodeInfo> inputInfo = session.getInputInfo();
            Map<String, NodeInfo> outputInfo = session.getOutputInfo();
            log.info("[{}] input={}, output={}", key, inputInfo.keySet(), outputInfo.keySet());
        } catch (OrtException e) {
            log.warn("[{}] Failed to read model signature: {}", key, e.getMessage());
        }
    }

    public synchronized void reloadModel(String key) {
        log.warn("Reloading model: {}", key);

        if (!MODEL_PATHS.containsKey(key)) {
            log.error("CRITICAL: Cannot reload unknown model key: {}", key);
            throw new StressTestException(StressTestErrorType.INVALID_MODEL_KEY);
        }

        OrtSession old = sessions.remove(key);
        if (old != null) {
            try {
                old.close();
            } catch (OrtException ignored) {
                // exception kısmı
            }
        }
        safeLoad(key);
    }

    public OrtSession getSession(String key) {
        OrtSession session = sessions.get(key);
        if (session == null) {
            throw new StressTestException(StressTestErrorType.MODEL_NOT_AVAILABLE);
        }
        return session;
    }


    public boolean isAvailable(String key) {
        return Boolean.TRUE.equals(modelAvailability.get(key));
    }

    @PreDestroy
    public void close() {
        sessions.forEach((key, session) -> {
            try {
                session.close();
            } catch (OrtException e) {
                log.warn("Error closing session for model [{}]: {}", key, e.getMessage());
            }
        });
        sessions.clear();

        modelLoaderExecutor.shutdown();
        try {
            if (!modelLoaderExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                modelLoaderExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            modelLoaderExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (env != null) {
            env.close();
        }
    }
}












