package com.akademi.finsight.decisionhistory.service.impl;

import com.akademi.finsight.fund.decision.dto.response.ManualScenarioResponse;
import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.ManualScenario;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import com.akademi.finsight.fund.decision.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.decision.repository.ManualScenarioRepository;
import com.akademi.finsight.fund.decision.service.ManualScenarioService;
import com.akademi.finsight.decisionhistory.dto.request.AttachStressTestRequest;
import com.akademi.finsight.decisionhistory.dto.response.DecisionRecordResponse;
import com.akademi.finsight.decisionhistory.mapper.DecisionRecordAssembler;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
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

import java.time.Instant;
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
    private DecisionRecordAssembler decisionRecordAssembler;

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

            ManualScenarioResponse manual = manualScenarioResponse(manualCreatedAt);
            AiRecommendation ai = aiRecommendation(RecommendationStatus.ACCEPTED, aiCreatedAt);

            when(userService.findByEmail(email)).thenReturn(user);
            when(manualScenarioService.getScenarioHistory(email, fundId)).thenReturn(List.of(manual));
            when(aiRecommendationRepository.findByUserIdAndFundIdAndStatusNotOrderByCreatedAtDesc(
                    user.getId(), fundId, RecommendationStatus.PENDING)).thenReturn(List.of(ai));
            when(decisionRecordAssembler.fromManualScenario(manual)).thenReturn(decisionRecord("MANUAL", manualCreatedAt));
            when(decisionRecordAssembler.fromAiRecommendation(ai)).thenReturn(decisionRecord("AI", aiCreatedAt));

            List<DecisionRecordResponse> history = decisionHistoryService.getHistory(email, fundId);

            assertEquals(2, history.size());
            assertEquals("AI", history.get(0).source());
            assertEquals(aiCreatedAt, history.get(0).createdAt());
            assertEquals("MANUAL", history.get(1).source());
            assertEquals(manualCreatedAt, history.get(1).createdAt());
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

    private ManualScenarioResponse manualScenarioResponse(Instant createdAt) {
        return new ManualScenarioResponse(
                UUID.randomUUID(),
                fundId,
                "Manuel senaryo notu",
                createdAt,
                List.of(),
                List.of(),
                null,
                null
        );
    }

    private DecisionRecordResponse decisionRecord(String source, Instant createdAt) {
        return new DecisionRecordResponse(
                UUID.randomUUID(),
                source,
                "ACCEPTED",
                null,
                null,
                createdAt,
                List.of(),
                List.of(),
                null,
                null
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
                                                          .build();
        ReflectionTestUtils.setField(recommendation, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(recommendation, "createdAt", createdAt);
        return recommendation;
    }
}
