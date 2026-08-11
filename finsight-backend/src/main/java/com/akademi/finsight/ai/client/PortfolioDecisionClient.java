package com.akademi.finsight.ai.client;

import com.akademi.finsight.ai.dto.*;
import com.akademi.finsight.ai.dto.request.DecisionRequest;
import com.akademi.finsight.ai.dto.request.PredictionRequest;
import com.akademi.finsight.ai.dto.response.ActionsResponseDto;
import com.akademi.finsight.ai.dto.response.DecisionResponse;
import com.akademi.finsight.ai.dto.response.PredictionResponse;
import com.akademi.finsight.ai.dto.response.StateSchemaResponseDto;
import com.akademi.finsight.ai.exception.PortfolioApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
public class PortfolioDecisionClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PortfolioDecisionClient(@Qualifier("portfolioApiRestClient") RestClient restClient,
                                   ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public DecisionResponse decide(DecisionRequest request) {
        return post("/api/v1/decisions", request, DecisionResponse.class);
    }

    public PredictionResponse predictLegacy(PredictionRequest request) {
        return post("/predict", request, PredictionResponse.class);
    }

    public StateSchemaResponseDto getStateSchema() {
        return restClient.get()
                .uri("/api/v1/state/schema")
                .retrieve()
                .body(StateSchemaResponseDto.class);
    }

    public ActionsResponseDto getActions() {
        return restClient.get()
                .uri("/api/v1/actions")
                .retrieve()
                .body(ActionsResponseDto.class);
    }

    public boolean isReady() {
        try {
            restClient.get().uri("/health/ready").retrieve().toBodilessEntity();
            return true;
        } catch (PortfolioApiException e) {
            if (e.isServiceUnavailable()) {
                return false;
            }
            throw e;
        }
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        String requestId = UUID.randomUUID().toString();
        return restClient.post()
                .uri(path)
                .header("X-Request-ID", requestId)
                .body(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        (req, res) -> {

                            byte[] bodyBytes = res.getBody().readAllBytes();
                            String responseBody = new String(bodyBytes);

                            ProblemDetails problem = null;
                            try {
                                problem = objectMapper.readValue(bodyBytes, ProblemDetails.class);
                            } catch (Exception e) {
                                System.err.println("ProblemDetails parse edilemedi: " + e.getMessage());
                            }
                            throw new PortfolioApiException(res.getStatusCode().value(), problem);
                        })
                .body(responseType);
    }
}