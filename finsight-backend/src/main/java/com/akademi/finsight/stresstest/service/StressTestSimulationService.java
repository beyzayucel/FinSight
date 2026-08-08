package com.akademi.finsight.stresstest.service;

import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.request.SaveStressTestDecisionRequestDto;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.enums.SimulationType;

import java.util.Optional;
import java.util.UUID;

public interface StressTestSimulationService {
    StressTestInferenceResponseDto runSimulation(String userEmail, String fundId, SimulationType simulationType, PortfolioDataDto currentPortfolio);
    Optional<StressTestInferenceResponseDto> getLatestSimulationResult(String userEmail, String fundId);

    Optional<StressTestInferenceResponseDto> getSimulationResultByPeriod(
            String userEmail,
            String fundId,
            String period
    );

    void saveDecisionRecord(String userEmail, SaveStressTestDecisionRequestDto requestDto);
}
