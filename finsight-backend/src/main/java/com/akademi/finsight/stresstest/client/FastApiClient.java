package com.akademi.finsight.stresstest.client;

import com.akademi.finsight.stresstest.dto.request.FastApiInferenceRequestDto;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.FastApiInferenceResponseDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiClient {
    private final WebClient.Builder webClientBuilder;

    //TODO: AI Servisinin istek atacağı url belirlendiğinde düzeltilecek
    @Value("${FASTAPI_BASE_URL:http://localhost:8000}")
    private String fastApiBaseUrl;

    public ModelInferenceResult predict(String scenarioKey, PortfolioDataDto portfolioData) {
        FastApiInferenceRequestDto requestPayload = new FastApiInferenceRequestDto(
                scenarioKey,
                portfolioData.initialValue(),
                portfolioData.assetWeights()
        );

        try {
            FastApiInferenceResponseDto response = webClientBuilder.build()
                    .post()
                    .uri(fastApiBaseUrl + "/api/v1/predict")
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(FastApiInferenceResponseDto.class)
                    .block(); // Senkron akış için bloklama yapıyoruz

            if (response == null) {
                throw new StressTestException(StressTestErrorType.MODEL_INFERENCE_ERROR);
            }

            return new ModelInferenceResult(response.expectedImpactRate(), response.postShockValue());

        } catch (Exception e) { // özel hata yapılmalı
            log.error("FastAPI AI Servis Hatasi [Scenario: {}]: {}", scenarioKey, e.getMessage(), e);
            throw new StressTestException(StressTestErrorType.MODEL_INFERENCE_ERROR, e);
        }
    }
}

