package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.ManualScenario;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import com.akademi.finsight.fund.decision.repository.AiRecommendationRepository;
import com.akademi.finsight.fund.decision.repository.ManualScenarioRepository;
import com.akademi.finsight.fund.dto.response.AdminDecisionRecordResponse;
import com.akademi.finsight.fund.entity.DecisionType;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDecisionServiceImpl Tests")
class AdminDecisionServiceImplTest {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    @Mock
    private AiRecommendationRepository aiRecommendationRepository;

    @Mock
    private ManualScenarioRepository manualScenarioRepository;

    @InjectMocks
    private AdminDecisionServiceImpl adminDecisionService;

    private User user;
    private Fund fund;
    private UUID userId;
    private final Instant now = Instant.parse("2026-08-11T09:00:00Z");

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                   .firstName("Beyza")
                   .lastName("Kaya")
                   .email("user@test.com")
                   .build();
        ReflectionTestUtils.setField(user, "id", userId);

        fund = Fund.builder()
                   .code("TIE")
                   .name("Tacirler Değişken Fon")
                   .build();
        ReflectionTestUtils.setField(fund, "id", UUID.randomUUID());
    }

    @Nested
    @DisplayName("getDecisionReport")
    class GetDecisionReport {

        @Test
        @DisplayName("should merge AI and manual decisions into a single newest-first report when no type filter is given")
        void shouldMergeAllDecisionTypes() {
            AiRecommendation accepted = aiRecommendation(RecommendationStatus.ACCEPTED, now.minus(3, ChronoUnit.HOURS));
            AiRecommendation rejected = aiRecommendation(RecommendationStatus.REJECTED, now.minus(1, ChronoUnit.HOURS));
            ManualScenario manual = manualScenario(now.minus(2, ChronoUnit.HOURS));

            stubAiRepository(List.of(rejected, accepted));
            stubManualRepository(List.of(manual));

            List<AdminDecisionRecordResponse> report = adminDecisionService.getDecisionReport(userId, null, null);

            assertEquals(3, report.size());
            assertEquals(DecisionType.AI_REJECTED, report.get(0).decisionType());
            assertEquals(DecisionType.MANUAL, report.get(1).decisionType());
            assertEquals(DecisionType.AI_APPROVED, report.get(2).decisionType());
        }

        @Test
        @DisplayName("should map user name, fund name and decision date onto the record")
        void shouldMapRecordFields() {
            Instant decisionDate = now.minus(1, ChronoUnit.HOURS);
            AiRecommendation accepted = aiRecommendation(RecommendationStatus.ACCEPTED, decisionDate);

            stubAiRepository(List.of(accepted));
            stubManualRepository(List.of());

            AdminDecisionRecordResponse record = adminDecisionService.getDecisionReport(userId, null, null).getFirst();

            assertEquals(accepted.getId(), record.id());
            assertEquals(decisionDate, record.decisionDate());
            assertEquals("Beyza Kaya", record.userName());
            assertEquals("Tacirler Değişken Fon", record.fundName());
            assertEquals(DecisionType.AI_APPROVED, record.decisionType());
        }

        @Test
        @DisplayName("should query only manual scenarios when the type filter is MANUAL")
        void shouldSkipAiRepositoryForManualFilter() {
            stubManualRepository(List.of(manualScenario(now)));

            List<AdminDecisionRecordResponse> report = adminDecisionService.getDecisionReport(userId, DecisionType.MANUAL, null);

            assertEquals(1, report.size());
            assertEquals(DecisionType.MANUAL, report.getFirst().decisionType());
            verifyNoInteractions(aiRecommendationRepository);
        }

        @Test
        @DisplayName("should query only AI recommendations and keep accepted ones when the type filter is AI_APPROVED")
        void shouldKeepOnlyAcceptedForApprovedFilter() {
            AiRecommendation accepted = aiRecommendation(RecommendationStatus.ACCEPTED, now.minus(1, ChronoUnit.HOURS));
            AiRecommendation rejected = aiRecommendation(RecommendationStatus.REJECTED, now);

            stubAiRepository(List.of(rejected, accepted));

            List<AdminDecisionRecordResponse> report =
                    adminDecisionService.getDecisionReport(userId, DecisionType.AI_APPROVED, null);

            assertEquals(1, report.size());
            assertEquals(accepted.getId(), report.getFirst().id());
            verifyNoInteractions(manualScenarioRepository);
        }

        @Test
        @DisplayName("should keep only rejected AI recommendations when the type filter is AI_REJECTED")
        void shouldKeepOnlyRejectedForRejectedFilter() {
            AiRecommendation accepted = aiRecommendation(RecommendationStatus.ACCEPTED, now.minus(1, ChronoUnit.HOURS));
            AiRecommendation rejected = aiRecommendation(RecommendationStatus.REJECTED, now);

            stubAiRepository(List.of(rejected, accepted));

            List<AdminDecisionRecordResponse> report =
                    adminDecisionService.getDecisionReport(userId, DecisionType.AI_REJECTED, null);

            assertEquals(1, report.size());
            assertEquals(rejected.getId(), report.getFirst().id());
            verifyNoInteractions(manualScenarioRepository);
        }

        @Test
        @DisplayName("should sort by createdAt descending in the database as well")
        void shouldDelegateSortingToRepositories() {
            stubAiRepository(List.of());
            stubManualRepository(List.of());

            adminDecisionService.getDecisionReport(userId, null, 7);

            verify(aiRecommendationRepository).findAll(ArgumentMatchers.<Specification<AiRecommendation>>any(), eq(NEWEST_FIRST));
            verify(manualScenarioRepository).findAll(ArgumentMatchers.<Specification<ManualScenario>>any(), eq(NEWEST_FIRST));
        }

        @Test
        @DisplayName("should return an empty report when the user has no decisions")
        void shouldReturnEmptyReport() {
            stubAiRepository(List.of());
            stubManualRepository(List.of());

            assertTrue(adminDecisionService.getDecisionReport(userId, null, 30).isEmpty());
        }
    }

    private void stubAiRepository(List<AiRecommendation> recommendations) {
        when(aiRecommendationRepository.findAll(ArgumentMatchers.<Specification<AiRecommendation>>any(), any(Sort.class)))
                .thenReturn(recommendations);
    }

    private void stubManualRepository(List<ManualScenario> scenarios) {
        when(manualScenarioRepository.findAll(ArgumentMatchers.<Specification<ManualScenario>>any(), any(Sort.class)))
                .thenReturn(scenarios);
    }

    private AiRecommendation aiRecommendation(RecommendationStatus status, Instant createdAt) {
        AiRecommendation recommendation = AiRecommendation.builder()
                                                          .user(user)
                                                          .fund(fund)
                                                          .status(status)
                                                          .rationale("AI gerekçesi")
                                                          .build();
        ReflectionTestUtils.setField(recommendation, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(recommendation, "createdAt", createdAt);
        return recommendation;
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
}
