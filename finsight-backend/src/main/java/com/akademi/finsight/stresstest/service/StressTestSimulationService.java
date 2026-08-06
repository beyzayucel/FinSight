package com.akademi.finsight.stresstest.service;

import com.akademi.finsight.stresstest.dto.request.StressTestInferenceRequestDto;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.entity.SimulationType;

import java.util.Optional;
import java.util.UUID;

public interface StressTestSimulationService {
    StressTestInferenceResponseDto runSimulation(String userEmail, UUID fundId, SimulationType simulationType);
    Optional<StressTestInferenceResponseDto> getLatestSimulationResult(String userEmail, UUID fundId);
}
