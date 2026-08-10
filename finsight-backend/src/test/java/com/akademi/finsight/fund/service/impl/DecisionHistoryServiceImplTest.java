package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.decision.dto.response.ManualScenarioResponse;
import com.akademi.finsight.fund.decision.dto.response.ManualScenarioStockWeightResponse;
import com.akademi.finsight.fund.decision.dto.response.ManualScenarioWeightResponse;
import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.AiRecommendationStockWeight;
import com.akademi.finsight.fund.decision.entity.AiRecommendationWeight;
import com.akademi.finsight.fund.decision.entity.AssetCategory;
import com.akademi.finsight.fund.decision.entity.ManualScenario;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import com.akademi.finsight.fund.decision.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.decision.repository.ManualScenarioRepository;
import com.akademi.finsight.fund.decision.service.ManualScenarioService;
import com.akademi.finsight.fund.dto.request.AttachStressTestRequest;
import com.akademi.finsight.fund.dto.response.DecisionRecordResponse;
import com.akademi.finsight.fund.dto.response.PerformanceMetricsResponse;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.PerformanceMetrics;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import com.akademi.finsight.stresstest.mapper.StressTestResponseAssembler;
import com.akademi.finsight.stresstest.repository.StressTestResultRepository;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.service.UserService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DecisionHistoryServiceImpl Tests")
class DecisionHistoryServiceImplTest {

    @Mock
    private ManualScenarioService manualScenarioService;

    @Mock
    private ManualScenarioRepository manualScenarioRepository;

    @Mock
    private AiRecommendationRepository aiRecommendationRepository;

    @Mock
    private StressTestResultRepository stressTestResultRepository;

    @Mock
    private StressTestResponseAssembler stressTestResponseAssembler;

    @Mock
    private UserService userService;

    @InjectMocks
    private DecisionHistoryServiceImpl decisionHistoryService;

    private User user;
    private Fund fund;
    private UUID fundId;
    private final String email = "user@test.com";
    private final Instant now = Instant.parse("2026-08-11T09:00:00Z");

    @BeforeEach
    void setUp() {
        fundId = UUID.randomUUID();

        user = User.builder()
                   .firstName("Beyza")
                   .lastName("Kaya")
                   .email(email)
                   .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        fund = Fund.builder()
                   .code("TIE")
                   .name("Tacirler Değişken Fon")
                   .build();
        ReflectionTestUtils.setField(fund, "id", fundId);
    }

    @Nested
    @DisplayName("getHistory")
    class GetHistory {

        @Test
        @DisplayName("should merge manual and AI decisions into a single newest-first list")
        void shouldMergeAndSortDecisions() {
            Instant manualCreatedAt = now.minus(2, ChronoUnit.HOURS);
            Instant aiCreatedAt = now.minus(1, ChronoUnit.HOURS);

            ManualScenarioResponse manual = manualScenarioResponse(manualCreatedAt, null);
            AiRecommendation ai = aiRecommendation(RecommendationStatus.ACCEPTED, aiCreatedAt);

            when(userService.findByEmail(email)).thenReturn(user);
            when(manualScenarioService.getScenarioHistory(email, fundId)).thenReturn(List.of(manual));
            when(aiRecommendationRepository.findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(
                    user.getId(), fundId, RecommendationStatus.PENDING)).thenReturn(List.of(ai));

            List<DecisionRecordResponse> history = decisionHistoryService.getHistory(email, fundId);

            assertEquals(2, history.size());
            assertEquals("AI", history.get(0).source());
            assertEquals(aiCreatedAt, history.get(0).createdAt());
            assertEquals("MANUAL", history.get(1).source());
            assertEquals(manualCreatedAt, history.get(1).createdAt());
        }

        @Test
        @DisplayName("should map manual scenario as an accepted MANUAL record without rationale")
        void shouldMapManualScenario() {
            StressTestInferenceResponseDto rawStressTest = StressTestInferenceResponseDto.builder()
                                                                                        .id(UUID.randomUUID())
                                                                                        .scenarioKey("INFLATION_SHOCK")
                                                                                        .build();
            StressTestInferenceResponseDto enrichedStressTest = rawStressTest.toBuilder()
                                                                            .llmComment("Yorum")
                                                                            .build();
            ManualScenarioResponse manual = manualScenarioResponse(now, rawStressTest);

            when(userService.findByEmail(email)).thenReturn(user);
            when(manualScenarioService.getScenarioHistory(email, fundId)).thenReturn(List.of(manual));
            when(aiRecommendationRepository.findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(
                    user.getId(), fundId, RecommendationStatus.PENDING)).thenReturn(List.of());
            when(stressTestResponseAssembler.withLlmComment(rawStressTest)).thenReturn(enrichedStressTest);

            DecisionRecordResponse record = decisionHistoryService.getHistory(email, fundId).getFirst();

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
        @DisplayName("should expose weights, stock weights and metrics of an accepted AI decision")
        void shouldMapAcceptedAiRecommendation() {
            AiRecommendation ai = aiRecommendation(RecommendationStatus.ACCEPTED, now);
            StressTestInferenceResponseDto stressTest = StressTestInferenceResponseDto.builder()
                                                                                     .id(UUID.randomUUID())
                                                                                     .scenarioKey("INFLATION_SHOCK")
                                                                                     .llmComment("Yorum")
                                                                                     .build();

            when(userService.findByEmail(email)).thenReturn(user);
            when(manualScenarioService.getScenarioHistory(email, fundId)).thenReturn(List.of());
            when(aiRecommendationRepository.findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(
                    user.getId(), fundId, RecommendationStatus.PENDING)).thenReturn(List.of(ai));
            when(stressTestResponseAssembler.toResponse(ai.getStressTestResult())).thenReturn(stressTest);

            DecisionRecordResponse record = decisionHistoryService.getHistory(email, fundId).getFirst();

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
            AiRecommendation ai = aiRecommendation(RecommendationStatus.REJECTED, now);

            when(userService.findByEmail(email)).thenReturn(user);
            when(manualScenarioService.getScenarioHistory(email, fundId)).thenReturn(List.of());
            when(aiRecommendationRepository.findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(
                    user.getId(), fundId, RecommendationStatus.PENDING)).thenReturn(List.of(ai));

            DecisionRecordResponse record = decisionHistoryService.getHistory(email, fundId).getFirst();

            assertEquals("REJECTED", record.status());
            assertTrue(record.weights().isEmpty());
            assertTrue(record.stockWeights().isEmpty());
            assertEquals("AI gerekçesi", record.rationale());
        }

        @Test
        @DisplayName("should return null metrics when the AI decision has no metrics stored")
        void shouldReturnNullMetricsWhenAbsent() {
            AiRecommendation ai = aiRecommendation(RecommendationStatus.ACCEPTED, now);
            ai.setMetrics(null);

            when(userService.findByEmail(email)).thenReturn(user);
            when(manualScenarioService.getScenarioHistory(email, fundId)).thenReturn(List.of());
            when(aiRecommendationRepository.findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(
                    user.getId(), fundId, RecommendationStatus.PENDING)).thenReturn(List.of(ai));

            DecisionRecordResponse record = decisionHistoryService.getHistory(email, fundId).getFirst();

            assertNull(record.metrics());
        }

        @Test
        @DisplayName("should return an empty history when the user has no decisions on the fund")
        void shouldReturnEmptyHistory() {
            when(userService.findByEmail(email)).thenReturn(user);
            when(manualScenarioService.getScenarioHistory(email, fundId)).thenReturn(List.of());
            when(aiRecommendationRepository.findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(
                    user.getId(), fundId, RecommendationStatus.PENDING)).thenReturn(List.of());

            assertTrue(decisionHistoryService.getHistory(email, fundId).isEmpty());
        }
    }

    @Nested
    @DisplayName("attachStressTestResult")
    class AttachStressTestResult {

        private StressTestResult stressTestResult;
        private AttachStressTestRequest request;

        @BeforeEach
        void setUpRequest() {
            stressTestResult = StressTestResult.builder()
                                               .user(user)
                                               .fund(fund)
                                               .build();
            ReflectionTestUtils.setField(stressTestResult, "id", UUID.randomUUID());
            request = new AttachStressTestRequest(fundId, stressTestResult.getId());
        }

        @Test
        @DisplayName("should attach the result to the manual scenario when it is the newest decision")
        void shouldAttachToManualWhenNewer() {
            ManualScenario manual = manualScenario(now);
            AiRecommendation ai = aiRecommendation(RecommendationStatus.ACCEPTED, now.minus(1, ChronoUnit.HOURS));

            stubLatestDecision(Optional.of(manual), Optional.of(ai));

            decisionHistoryService.attachStressTestResult(email, request);

            assertSame(stressTestResult, manual.getStressTestResult());
            verify(manualScenarioRepository).save(manual);
            verify(aiRecommendationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should attach the result to the AI decision when it is the newest decision")
        void shouldAttachToAiWhenNewer() {
            ManualScenario manual = manualScenario(now.minus(1, ChronoUnit.HOURS));
            AiRecommendation ai = aiRecommendation(RecommendationStatus.ACCEPTED, now);

            stubLatestDecision(Optional.of(manual), Optional.of(ai));

            decisionHistoryService.attachStressTestResult(email, request);

            assertSame(stressTestResult, ai.getStressTestResult());
            verify(aiRecommendationRepository).save(ai);
            verify(manualScenarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("should attach the result to the manual scenario when no AI decision exists")
        void shouldAttachToManualWhenNoAiDecision() {
            ManualScenario manual = manualScenario(now);

            stubLatestDecision(Optional.of(manual), Optional.empty());

            decisionHistoryService.attachStressTestResult(email, request);

            assertSame(stressTestResult, manual.getStressTestResult());
            verify(manualScenarioRepository).save(manual);
        }

        @Test
        @DisplayName("should attach the result to the AI decision when no manual scenario exists")
        void shouldAttachToAiWhenNoManualScenario() {
            AiRecommendation ai = aiRecommendation(RecommendationStatus.REJECTED, now);

            stubLatestDecision(Optional.empty(), Optional.of(ai));

            decisionHistoryService.attachStressTestResult(email, request);

            assertSame(stressTestResult, ai.getStressTestResult());
            verify(aiRecommendationRepository).save(ai);
        }

        @Test
        @DisplayName("should throw NO_DECISION_TO_ATTACH when the fund has no decision yet")
        void shouldThrowWhenNoDecisionExists() {
            stubLatestDecision(Optional.empty(), Optional.empty());

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    decisionHistoryService.attachStressTestResult(email, request));

            assertEquals(FundErrorType.NO_DECISION_TO_ATTACH, ex.getErrorType());
            verify(manualScenarioRepository, never()).save(any());
            verify(aiRecommendationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw RESULT_NOT_FOUND when the stress test result does not exist")
        void shouldThrowWhenResultNotFound() {
            when(userService.findByEmail(email)).thenReturn(user);
            when(stressTestResultRepository.findById(request.stressTestResultId())).thenReturn(Optional.empty());

            StressTestException ex = assertThrows(StressTestException.class, () ->
                    decisionHistoryService.attachStressTestResult(email, request));

            assertEquals(StressTestErrorType.RESULT_NOT_FOUND, ex.getErrorType());
        }

        @Test
        @DisplayName("should throw RESULT_ACCESS_DENIED when the result belongs to another user")
        void shouldThrowWhenResultBelongsToAnotherUser() {
            User otherUser = User.builder().email("other@test.com").build();
            ReflectionTestUtils.setField(otherUser, "id", UUID.randomUUID());
            stressTestResult.setUser(otherUser);

            when(userService.findByEmail(email)).thenReturn(user);
            when(stressTestResultRepository.findById(request.stressTestResultId()))
                    .thenReturn(Optional.of(stressTestResult));

            StressTestException ex = assertThrows(StressTestException.class, () ->
                    decisionHistoryService.attachStressTestResult(email, request));

            assertEquals(StressTestErrorType.RESULT_ACCESS_DENIED, ex.getErrorType());
            verify(manualScenarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw RESULT_ACCESS_DENIED when the result belongs to another fund")
        void shouldThrowWhenResultBelongsToAnotherFund() {
            Fund otherFund = Fund.builder().code("AFA").name("Ak Portföy").build();
            ReflectionTestUtils.setField(otherFund, "id", UUID.randomUUID());
            stressTestResult.setFund(otherFund);

            when(userService.findByEmail(email)).thenReturn(user);
            when(stressTestResultRepository.findById(request.stressTestResultId()))
                    .thenReturn(Optional.of(stressTestResult));

            StressTestException ex = assertThrows(StressTestException.class, () ->
                    decisionHistoryService.attachStressTestResult(email, request));

            assertEquals(StressTestErrorType.RESULT_ACCESS_DENIED, ex.getErrorType());
            verify(aiRecommendationRepository, never()).save(any());
        }

        private void stubLatestDecision(Optional<ManualScenario> manual, Optional<AiRecommendation> ai) {
            when(userService.findByEmail(email)).thenReturn(user);
            when(stressTestResultRepository.findById(request.stressTestResultId()))
                    .thenReturn(Optional.of(stressTestResult));
            when(manualScenarioRepository.findFirstByUserIdAndFundIdOrderByCreatedAtDesc(user.getId(), fundId))
                    .thenReturn(manual);
            when(aiRecommendationRepository.findFirstByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(
                    user.getId(), fundId, RecommendationStatus.PENDING)).thenReturn(ai);
        }
    }

    private ManualScenarioResponse manualScenarioResponse(Instant createdAt, StressTestInferenceResponseDto stressTest) {
        return new ManualScenarioResponse(
                UUID.randomUUID(),
                fundId,
                "Manuel senaryo notu",
                createdAt,
                List.of(new ManualScenarioWeightResponse(AssetCategory.STOCK, BigDecimal.valueOf(50), BigDecimal.valueOf(45))),
                List.of(new ManualScenarioStockWeightResponse("THYAO", BigDecimal.valueOf(20), BigDecimal.valueOf(18))),
                new PerformanceMetricsResponse(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(-3),
                        BigDecimal.valueOf(0.9), 30, LocalDate.of(2026, 8, 3)),
                stressTest
        );
    }

    private ManualScenario manualScenario(Instant createdAt) {
        ManualScenario scenario = ManualScenario.builder()
                                                .user(user)
                                                .fund(fund)
                                                .note("Manuel senaryo notu")
                                                .build();
        ReflectionTestUtils.setField(scenario, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(scenario, "createdAt", createdAt);
        return scenario;
    }

    private AiRecommendation aiRecommendation(RecommendationStatus status, Instant createdAt) {
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
        ReflectionTestUtils.setField(recommendation, "createdAt", createdAt);

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
