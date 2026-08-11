package com.akademi.finsight.stresstest;

import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.decisionhistory.service.DecisionHistoryService;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.engine.StressTestCalculationEngine;
import com.akademi.finsight.stresstest.engine.StressTestStrategyFactory;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import com.akademi.finsight.stresstest.mapper.StressTestMapper;
import com.akademi.finsight.stresstest.repository.StressTestResultRepository;
import com.akademi.finsight.stresstest.service.helper.PortfolioDataBuilder;
import com.akademi.finsight.stresstest.service.impl.StressTestSimulationServiceImpl;
import com.akademi.finsight.stresstest.util.StressTestPeriodParser;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StressTestSimulationServiceImpl Tests")
class StressTestSimulationServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private FundRepository fundRepository;
    @Mock private StressTestStrategyFactory strategyFactory;
    @Mock private StressTestResultRepository stressTestResultRepository;
    @Mock private DecisionHistoryService decisionHistoryService;
    @Mock private PortfolioDataBuilder portfolioDataBuilder;
    @Mock private StressTestPeriodParser periodParser;
    @Mock private StressTestMapper stressTestMapper;
    @Mock private StressTestCalculationEngine strategyEngine;

    @InjectMocks
    private StressTestSimulationServiceImpl stressTestSimulationService;

    private User mockUser;
    private Fund mockFund;
    private PortfolioDataDto inputPortfolio;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@example.com");

        mockFund = new Fund();
        mockFund.setCode("TIE");

        inputPortfolio = PortfolioDataDto.builder()
                .initialValue(BigDecimal.valueOf(100000))
                .assetWeights(Map.of("EQUITY", 0.50f, "BOND", 0.50f))
                .build();
    }

    @Nested
    @DisplayName("runSimulation")
    class RunSimulation {

        @Test
        @DisplayName("should build simulation/benchmark portfolios, run inference for all three, and persist result")
        void shouldCalculateAndSaveSuccessfully() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
            when(fundRepository.findByCode("TIE")).thenReturn(Optional.of(mockFund));

            PortfolioDataDto simulationPortfolio = PortfolioDataDto.builder()
                    .initialValue(BigDecimal.valueOf(105000))
                    .assetWeights(Map.of("EQUITY", 0.40f, "BOND", 0.30f, "FX", 0.15f, "CASH", 0.15f))
                    .build();
            PortfolioDataDto benchmarkPortfolio = PortfolioDataDto.builder()
                    .initialValue(BigDecimal.valueOf(102000))
                    .assetWeights(Map.of("EQUITY", 0.50f, "BOND", 0.25f, "FX", 0.15f, "CASH", 0.10f))
                    .build();
            when(portfolioDataBuilder.buildSimulationPortfolio("test@example.com", "TIE", 30, inputPortfolio.initialValue()))
                    .thenReturn(simulationPortfolio);
            when(portfolioDataBuilder.buildBenchmarkPortfolio("TIE", 30, inputPortfolio.initialValue()))
                    .thenReturn(benchmarkPortfolio);

            when(strategyFactory.getEngine(ExecutionStrategyType.MANUAL_RULE)).thenReturn(strategyEngine);
            when(strategyEngine.runInference(eq(SimulationType.EQUITY_SHOCK.name()), any(PortfolioDataDto.class)))
                    .thenReturn(ModelInferenceResult.builder()
                            .expectedImpactRate(BigDecimal.valueOf(-0.05))
                            .postShockValue(BigDecimal.valueOf(95000))
                            .build());

            StressTestResult stressTestResult = StressTestResult.builder()
                    .user(mockUser)
                    .fund(mockFund)
                    .simulationType(SimulationType.EQUITY_SHOCK)
                    .build();
            when(stressTestMapper.createStressTestResult(mockUser, mockFund, SimulationType.EQUITY_SHOCK))
                    .thenReturn(stressTestResult);

            StressTestResult savedResult = StressTestResult.builder()
                    .user(mockUser)
                    .fund(mockFund)
                    .simulationType(SimulationType.EQUITY_SHOCK)
                    .build();
            when(stressTestResultRepository.save(stressTestResult)).thenReturn(savedResult);

            StressTestInferenceResponseDto expectedResponse = StressTestInferenceResponseDto.builder()
                    .scenarioKey(SimulationType.EQUITY_SHOCK.name())
                    .build();
            when(stressTestMapper.toInferenceResponseDto(savedResult)).thenReturn(expectedResponse);

            StressTestInferenceResponseDto response = stressTestSimulationService.runSimulation(
                    "test@example.com", "TIE", SimulationType.EQUITY_SHOCK, inputPortfolio, 30);

            assertEquals(expectedResponse, response);
            // Current + simulation + benchmark portfolios each go through inference once.
            verify(strategyEngine, times(3)).runInference(eq(SimulationType.EQUITY_SHOCK.name()), any(PortfolioDataDto.class));
            verify(stressTestResultRepository, times(1)).save(stressTestResult);
        }

        @Test
        @DisplayName("should throw INVALID_INPUT when portfolio has no asset weights")
        void shouldThrowWhenPortfolioInvalid() {
            PortfolioDataDto invalidPortfolio = PortfolioDataDto.builder()
                    .initialValue(BigDecimal.valueOf(100000))
                    .assetWeights(Map.of())
                    .build();

            StressTestException exception = assertThrows(StressTestException.class, () ->
                    stressTestSimulationService.runSimulation(
                            "test@example.com", "TIE", SimulationType.EQUITY_SHOCK, invalidPortfolio, 30));

            assertEquals(StressTestErrorType.INVALID_INPUT, exception.getErrorType());
        }

        @Test
        @DisplayName("should throw USER_NOT_FOUND when user email does not resolve")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(UserException.class, () ->
                    stressTestSimulationService.runSimulation(
                            "missing@example.com", "TIE", SimulationType.EQUITY_SHOCK, inputPortfolio, 30));
        }

        @Test
        @DisplayName("should throw FundNotFoundException when fund code/id does not resolve")
        void shouldThrowWhenFundNotFound() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
            when(fundRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

            assertThrows(FundNotFoundException.class, () ->
                    stressTestSimulationService.runSimulation(
                            "test@example.com", "UNKNOWN", SimulationType.EQUITY_SHOCK, inputPortfolio, 30));
        }
    }

    @Nested
    @DisplayName("getLatestSimulationResult")
    class GetLatestSimulationResult {

        @Test
        @DisplayName("should map the latest persisted result for the user/fund")
        void shouldReturnLatestResult() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
            when(fundRepository.findByCode("TIE")).thenReturn(Optional.of(mockFund));

            UUID userId = UUID.randomUUID();
            UUID fundId = UUID.randomUUID();
            org.springframework.test.util.ReflectionTestUtils.setField(mockUser, "id", userId);
            org.springframework.test.util.ReflectionTestUtils.setField(mockFund, "id", fundId);

            StressTestResult latest = StressTestResult.builder()
                    .user(mockUser).fund(mockFund).simulationType(SimulationType.EQUITY_SHOCK).build();
            when(stressTestResultRepository.findFirstByUserIdAndFundIdOrderByCreatedAtDesc(userId, fundId))
                    .thenReturn(Optional.of(latest));

            StressTestInferenceResponseDto mapped = StressTestInferenceResponseDto.builder()
                    .scenarioKey(SimulationType.EQUITY_SHOCK.name())
                    .build();
            when(stressTestMapper.toInferenceResponseDto(latest)).thenReturn(mapped);

            Optional<StressTestInferenceResponseDto> result =
                    stressTestSimulationService.getLatestSimulationResult("test@example.com", "TIE");

            assertEquals(Optional.of(mapped), result);
        }
    }
}
