package com.akademi.finsight.stresstest.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.stresstest.controller.api.StressTestApi;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.request.SaveStressTestDecisionRequestDto;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.service.StressTestSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
public class StressTestController extends BaseController implements StressTestApi {

    private final StressTestSimulationService stressTestSimulationService;

    @Override
    public ResponseEntity<ApiStandardResponse<StressTestInferenceResponseDto>> runSimulation(
            String userEmail,
            String fundId,
            SimulationType simulationType,
            @RequestBody PortfolioDataDto portfolioDataDto) {

        StressTestInferenceResponseDto response = stressTestSimulationService
                .runSimulation(userEmail, fundId, simulationType, portfolioDataDto);

        return ok(response);
    }

    @Override
    public ResponseEntity<ApiStandardResponse<StressTestInferenceResponseDto>> getLatestSimulationResult(
            String email,
            String fundId) {

        Optional<StressTestInferenceResponseDto> response = stressTestSimulationService
                .getLatestSimulationResult(email, fundId);

        return response
                .map(this::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<ApiStandardResponse<StressTestInferenceResponseDto>> getSimulationResultByPeriod(
            String email,
            String fundId,
            String daysAgo) {

        Optional<StressTestInferenceResponseDto> response = stressTestSimulationService
                .getSimulationResultByPeriod(email, fundId, daysAgo);

        return response
                .map(this::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> saveDecisionRecord(
            String userEmail,
            @RequestBody SaveStressTestDecisionRequestDto requestDto) {

        stressTestSimulationService.saveDecisionRecord(userEmail, requestDto);
        return ok();
    }
}