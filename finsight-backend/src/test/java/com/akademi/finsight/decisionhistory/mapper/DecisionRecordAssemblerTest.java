package com.akademi.finsight.decisionhistory.mapper;

import com.akademi.finsight.decisionhistory.dto.response.DecisionRecordResponse;
import com.akademi.finsight.fund.decision.dto.response.ManualScenarioResponse;
import com.akademi.finsight.fund.decision.dto.response.ManualScenarioStockWeightResponse;
import com.akademi.finsight.fund.decision.dto.response.ManualScenarioWeightResponse;
import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.AiRecommendationStockWeight;
import com.akademi.finsight.fund.decision.entity.AiRecommendationWeight;
import com.akademi.finsight.fund.decision.entity.AssetCategory;
import com.akademi.finsight.fund.decision.entity.ManualScenario;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import com.akademi.finsight.fund.decision.mapper.ManualScenarioMapper;
import com.akademi.finsight.fund.dto.response.PerformanceMetricsResponse;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.PerformanceMetrics;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.mapper.StressTestResponseAssembler;
import com.akademi.finsight.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DecisionRecordAssembler Tests")
class DecisionRecordAssemblerTest {

    @Mock
    private ManualScenarioMapper manualScenarioMapper;

    @Mock
    private StressTestResponseAssembler stressTestResponseAssembler;

    @InjectMocks
    private DecisionRecordAssembler decisionRecordAssembler;

    private User user;
    private Fund fund;
    private UUID fundId;
    private final Instant now = Instant.parse("2026-08-11T09:00:00Z");

    @BeforeEach
    void setUp() {
        fundId = UUID.randomUUID();

        user = User.builder()
                   .firstName("Beyza")
                   .lastName("Kaya")
                   .email("user@test.com")
                   .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        fund = Fund.builder()
                   .code("TIE")
                   .name("Tacirler Değişken Fon")
                   .build();
        ReflectionTestUtils.setField(fund, "id", fundId);
    }

    @Nested
    @DisplayName("fromManualScenario")
    class FromManualScenario {

        @Test
        @DisplayName("should map a manual scenario as an accepted MANUAL record without rationale")
        void shouldMapManualScenarioResponse() {
            StressTestInferenceResponseDto rawStressTest = StressTestInferenceResponseDto.builder()
                                                                                         .id(UUID.randomUUID())
                                                                                         .scenarioKey("INFLATION_SHOCK")
                                                                                         .build();
            StressTestInferenceResponseDto enrichedStressTest = rawStressTest.toBuilder()
                                                                             .llmComment("Yorum")
                                                                             .build();
            ManualScenarioResponse manual = manualScenarioResponse(rawStressTest);

            when(stressTestResponseAssembler.withLlmComment(rawStressTest)).thenReturn(enrichedStressTest);

            DecisionRecordResponse record = decisionRecordAssembler.fromManualScenario(manual);

            assertEquals(manual.id(), record.id());
            assertEquals("MANUAL", record.source());
            assertEquals("ACCEPTED", record.status());
            assertNull(record.rationale());
            assertEquals("Manuel senaryo notu", record.note());
            assertSame(manual.weights(), record.weights());
            assertSame(manual.stockWeights(), record.stockWeights());
            assertSame(manual.metrics(), record.metrics());
            assertSame(enrichedStressTest, record.stressTest());
        }

        @Test
        @DisplayName("should map a manual scenario entity through the scenario mapper")
        void shouldMapManualScenarioEntity() {
            ManualScenario scenario = ManualScenario.builder()
                                                    .user(user)
                                                    .fund(fund)
                                                    .note("Manuel senaryo notu")
                                                    .build();
            ReflectionTestUtils.setField(scenario, "id", UUID.randomUUID());
            ManualScenarioResponse mapped = manualScenarioResponse(null);

            when(manualScenarioMapper.toResponse(scenario)).thenReturn(mapped);

            DecisionRecordResponse record = decisionRecordAssembler.fromManualScenario(scenario);

            assertEquals(mapped.id(), record.id());
            assertEquals("MANUAL", record.source());
            assertEquals("ACCEPTED", record.status());
            assertEquals("Manuel senaryo notu", record.note());
        }
    }

    @Nested
    @DisplayName("fromAiRecommendation")
    class FromAiRecommendation {

        @Test
        @DisplayName("should expose weights, stock weights and metrics of an accepted AI decision")
        void shouldMapAcceptedAiRecommendation() {
            AiRecommendation ai = aiRecommendation(RecommendationStatus.ACCEPTED);
            StressTestInferenceResponseDto stressTest = StressTestInferenceResponseDto.builder()
                                                                                      .id(UUID.randomUUID())
                                                                                      .scenarioKey("INFLATION_SHOCK")
                                                                                      .llmComment("Yorum")
                                                                                      .build();

            when(stressTestResponseAssembler.toResponse(ai.getStressTestResult())).thenReturn(stressTest);

            DecisionRecordResponse record = decisionRecordAssembler.fromAiRecommendation(ai);

            assertEquals("AI", record.source());
            assertEquals("ACCEPTED", record.status());
            assertEquals("AI gerekçesi", record.rationale());
            assertEquals("Kullanıcı notu", record.note());

            ManualScenarioWeightResponse weight = record.weights().getFirst();
            assertEquals(AssetCategory.STOCK, weight.category());
            assertEquals(BigDecimal.valueOf(55), weight.targetWeight());
            assertEquals(BigDecimal.valueOf(40), weight.currentWeight());

            ManualScenarioStockWeightResponse stockWeight = record.stockWeights().getFirst();
            assertEquals("THYAO", stockWeight.assetCode());
            assertEquals(BigDecimal.valueOf(30), stockWeight.targetWeight());
            assertEquals(BigDecimal.valueOf(25), stockWeight.currentWeight());

            PerformanceMetricsResponse metrics = record.metrics();
            assertNotNull(metrics);
            assertEquals(BigDecimal.valueOf(12.5), metrics.totalReturnPct());
            assertEquals(BigDecimal.valueOf(2.5), metrics.benchmarkDiffPct());
            assertEquals(BigDecimal.valueOf(-4.2), metrics.maxDrawdownPct());
            assertEquals(BigDecimal.valueOf(1.1), metrics.dailyVolatilityPct());
            assertEquals(30, metrics.analysisWindowDays());
            // T-8: metrikler karar tarihinden değil, Infina'nın veri tarihinden gelir.
            assertEquals(LocalDate.of(2026, 8, 3), metrics.dataDate());

            assertSame(stressTest, record.stressTest());
        }

        @Test
        @DisplayName("should hide weights of a rejected AI decision but keep its rationale (K4)")
        void shouldHideWeightsOfRejectedAiRecommendation() {
            AiRecommendation ai = aiRecommendation(RecommendationStatus.REJECTED);

            DecisionRecordResponse record = decisionRecordAssembler.fromAiRecommendation(ai);

            assertEquals("REJECTED", record.status());
            assertTrue(record.weights().isEmpty());
            assertTrue(record.stockWeights().isEmpty());
            assertEquals("AI gerekçesi", record.rationale());
        }

        @Test
        @DisplayName("should return null metrics when the AI decision has no metrics stored")
        void shouldReturnNullMetricsWhenAbsent() {
            AiRecommendation ai = aiRecommendation(RecommendationStatus.ACCEPTED);
            ai.setMetrics(null);

            DecisionRecordResponse record = decisionRecordAssembler.fromAiRecommendation(ai);

            assertNull(record.metrics());
        }
    }

    private ManualScenarioResponse manualScenarioResponse(StressTestInferenceResponseDto stressTest) {
        return new ManualScenarioResponse(
                UUID.randomUUID(),
                fundId,
                "Manuel senaryo notu",
                now,
                List.of(new ManualScenarioWeightResponse(AssetCategory.STOCK, BigDecimal.valueOf(50), BigDecimal.valueOf(45))),
                List.of(new ManualScenarioStockWeightResponse("THYAO", BigDecimal.valueOf(20), BigDecimal.valueOf(18))),
                new PerformanceMetricsResponse(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(-3),
                        BigDecimal.valueOf(0.9), 30, LocalDate.of(2026, 8, 3)),
                stressTest
        );
    }

    private AiRecommendation aiRecommendation(RecommendationStatus status) {
        AiRecommendation recommendation = AiRecommendation.builder()
                                                          .user(user)
                                                          .fund(fund)
                                                          .status(status)
                                                          .rationale("AI gerekçesi")
                                                          .note("Kullanıcı notu")
                                                          .metrics(PerformanceMetrics.builder()
                                                                                     .totalReturnPct(BigDecimal.valueOf(12.5))
                                                                                     .benchmarkDiffPct(BigDecimal.valueOf(2.5))
                                                                                     .maxDrawdownPct(BigDecimal.valueOf(-4.2))
                                                                                     .dailyVolatilityPct(BigDecimal.valueOf(1.1))
                                                                                     .analysisWindowDays(30)
                                                                                     .dataDate(LocalDate.of(2026, 8, 3))
                                                                                     .build())
                                                          .build();
        ReflectionTestUtils.setField(recommendation, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(recommendation, "createdAt", now);

        recommendation.addWeight(AiRecommendationWeight.builder()
                                                       .category(AssetCategory.STOCK)
                                                       .recommendedWeight(BigDecimal.valueOf(55))
                                                       .currentWeight(BigDecimal.valueOf(40))
                                                       .build());
        recommendation.addStockWeight(AiRecommendationStockWeight.builder()
                                                                 .assetCode("THYAO")
                                                                 .recommendedWeight(BigDecimal.valueOf(30))
                                                                 .currentWeight(BigDecimal.valueOf(25))
                                                                 .build());
        return recommendation;
    }
}
