package com.akademi.finsight.stresstest;

import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioCurve;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioMetrics;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.performancecomparison.service.impl.ScenarioResolver;
import com.akademi.finsight.fund.performancecomparison.service.impl.ScenarioResolver.ResolvedScenario;
import com.akademi.finsight.fund.performancecomparison.dto.response.ScenarioSource;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.engine.StressTestCalculationEngine;
import com.akademi.finsight.stresstest.engine.StressTestStrategyFactory;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.mapper.StressTestResponseAssembler;
import com.akademi.finsight.stresstest.repository.StressTestResultRepository;
import com.akademi.finsight.stresstest.service.impl.StressTestSimulationServiceImpl;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StressTestSimulationServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private FundRepository fundRepository;
    @Mock private StressTestStrategyFactory strategyFactory;
    @Mock private StressTestResultRepository stressTestResultRepository;
    @Mock private StressTestResponseAssembler stressTestResponseAssembler;
    @Mock private FundPeriodMetricService fundPeriodMetricService;
    @Mock private PortfolioSimulationCalculationService simulationCalculationService;
    @Mock private ScenarioResolver scenarioResolver;
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

    @Test
    void runSimulation_ShouldCalculateAndSaveSuccessfully() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(fundRepository.findByCode(anyString())).thenReturn(Optional.of(mockFund));

        // ScenarioResolver Mock
        Map<AssetCategory, BigDecimal> mockWeights = Map.of(
                AssetCategory.STOCK, BigDecimal.valueOf(50),
                AssetCategory.FUND, BigDecimal.valueOf(50)
        );
        ResolvedScenario mockScenario = new ResolvedScenario(mockWeights, ScenarioSource.AI);
        when(scenarioResolver.resolve(anyString())).thenReturn(Optional.of(mockScenario));
        when(scenarioResolver.resolve(anyString())).thenReturn(Optional.of(mockScenario));

        // FundPeriodMetricService Mock
        // FundPeriodMetricService Mock
        FundPeriodMetricResponse mockMetric = new FundPeriodMetricResponse(
                UUID.randomUUID(),               // 1. fundId (UUID)
                UUID.randomUUID(),                           // 2. fundCode (String)
                LocalDate.now(),                 // 3. dataDate (LocalDate)
                "P30D",                          // 4. period (String)
                BigDecimal.valueOf(100000),      // 5. totalValue (BigDecimal)
                BigDecimal.valueOf(95000),       // 6. previousTotalValue (BigDecimal)
                LocalDate.now().minusDays(1),    // 7. previousDate (LocalDate)
                BigDecimal.valueOf(1.5),         // 8. dailyReturn (BigDecimal)
                BigDecimal.valueOf(10.0),        // 9. cumulativeReturn (BigDecimal)
                BigDecimal.valueOf(8.0),         // 10. benchmarkReturn (BigDecimal)
                BigDecimal.valueOf(2.0),         // 11. benchmarkDiffBps (BigDecimal)
                Instant.now(),                   // 12. fetchedAt (Instant)
                Instant.now()                    // 13. createdAt (Instant)
        );

        when(fundPeriodMetricService.getLatestByFundCodeAndPeriod(anyString(), anyString()))
                .thenReturn(mockMetric);

        // Simulation Calculation Mock
        PortfolioCurve mockCurve = new PortfolioCurve(
                List.of(), new PortfolioMetrics(BigDecimal.valueOf(105000), BigDecimal.valueOf(5.0), BigDecimal.ZERO, BigDecimal.ZERO)
        );
        when(simulationCalculationService.calculateSimulation(anyString(), anyInt(), any())).thenReturn(mockCurve);

        // Engine Mock
        when(strategyFactory.getEngine(any(ExecutionStrategyType.class))).thenReturn(strategyEngine);

        when(strategyEngine.runInference(anyString(), any(PortfolioDataDto.class)))
                .thenReturn(ModelInferenceResult.builder()
                        .expectedImpactRate(BigDecimal.valueOf(-0.05))
                        .postShockValue(BigDecimal.valueOf(95000))
                        .build());

        // Save Mock
        StressTestResult mockSavedResult = new StressTestResult();
        when(stressTestResultRepository.save(any(StressTestResult.class))).thenReturn(mockSavedResult);
        when(stressTestResponseAssembler.toResponse(any())).thenReturn(StressTestInferenceResponseDto.builder().build());

        // When
        StressTestInferenceResponseDto response = stressTestSimulationService.runSimulation(
                "test@example.com", "TIE", SimulationType.EQUITY_SHOCK, inputPortfolio, 30
        );


// Çıktıyı konsola basmak için ekle:
        System.out.println("=== STRESS TEST SIMULATION RESPONSE ===");
        System.out.println(response);

// Then
        assertNotNull(response);

        // Then
        assertNotNull(response);
        verify(stressTestResultRepository, times(1)).save(any(StressTestResult.class));
        verify(simulationCalculationService, times(1)).calculateSimulation(eq("TIE"), eq(30), any());
    }
}