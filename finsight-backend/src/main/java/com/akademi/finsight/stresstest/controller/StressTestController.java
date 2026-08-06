package com.akademi.finsight.stresstest.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.stresstest.controller.api.StressTestApi;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.entity.SimulationType;
import com.akademi.finsight.stresstest.service.StressTestSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
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
            @AuthenticationPrincipal UserDetails userDetails,
            UUID fundId,
            SimulationType simulationType) {

        StressTestInferenceResponseDto response = stressTestSimulationService.runSimulation(
                userDetails.getUsername(),
                fundId,
                simulationType
        );

        return ok(response);
    }

    @Override
    public ResponseEntity<ApiStandardResponse<StressTestInferenceResponseDto>> getLatestSimulationResult(
            @AuthenticationPrincipal UserDetails userDetails,
            UUID fundId) {

        Optional<StressTestInferenceResponseDto> response = stressTestSimulationService
                .getLatestSimulationResult(userDetails.getUsername(), fundId);

        return response
                .map(this::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
