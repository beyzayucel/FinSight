package com.akademi.finsight.fund.decision.service.impl;

import com.akademi.finsight.fund.config.FundProperties;
import com.akademi.finsight.fund.decision.converter.FundDistributionConverter;
import com.akademi.finsight.ai.model.dto.request.FundModelInputRequest;
import com.akademi.finsight.fund.decision.dto.response.AIRecommendationResponse;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.dto.response.FundStockBreakdownResponse;
import com.akademi.finsight.fund.dto.response.FundStockWeightResponse;
import com.akademi.finsight.fund.entity.*;
import com.akademi.finsight.fund.decision.entity.AiRecommendation;
import com.akademi.finsight.fund.decision.entity.AiRecommendationStockWeight;
import com.akademi.finsight.fund.decision.entity.AiRecommendationWeight;
import com.akademi.finsight.fund.decision.entity.RecommendationStatus;
import com.akademi.finsight.fund.exception.AiRecommendationNotFoundException;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.fund.decision.mapper.AiRecommendationMapper;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.decision.repository.AiRecommendationRepository;
import com.akademi.finsight.ai.model.repository.FundPriceDataRepository;
import com.akademi.finsight.ai.model.repository.MarketDataRepository;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import com.akademi.finsight.fund.service.FundService;
import com.akademi.finsight.fund.service.FundStockAllocationService;
import com.akademi.finsight.fund.service.OnnxModelService;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiRecommendationServiceImpl Tests")
class AiRecommendationServiceImplTest {

    @Mock
    private FundProperties fundProperties;

    @Mock
    private FundService fundService;

    @Mock
    private FundDistributionService fundDistributionService;

    @Mock
    private FundStockAllocationService fundStockAllocationService;

    @Mock
    private FundPeriodMetricService fundPeriodMetricService;

    @Mock
    private AiRecommendationRepository aiRecommendationRepository;

    @Mock
    private OnnxModelService onnxModelService;

    @Mock
    private UserService userService;

    @Mock
    private FundDistributionConverter fundDistributionConverter;

    @Mock
    private AiRecommendationMapper aiRecommendationMapper;

    @Mock
    private MarketDataRepository marketDataRepository;

    @Mock
    private FundPriceDataRepository fundPriceDataRepository;

    @Mock
    private PortfolioSimulationCalculationService portfolioSimulationCalculationService;

    @InjectMocks
    private AiRecommendationServiceImpl aiRecommendationService;

    private User user;
    private FundResponse fundResponse;
    private UUID fundId;
    private final String email = "user@test.com";

    @BeforeEach
    void setUp() {
        fundId = UUID.randomUUID();
        user = User.builder()
                .email(email)
                .firstName("Beyza")
                .lastName("Test")
                .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        fundResponse = new FundResponse(fundId, "TIE", "Tacirler Portföy Değişken Fon", Instant.now());
    }

    @Nested
    @DisplayName("getPendingRecommendation")
    class GetPendingRecommendation {

        @Test
        @DisplayName("should return existing pending recommendation when not expired")
        void shouldReturnExistingPendingRecommendationWhenValid() {
            FundProperties.Recommendation recProps = new FundProperties.Recommendation();
            recProps.setTtlHours(4);
            when(fundProperties.getRecommendation()).thenReturn(recProps);

            when(userService.findByEmail(email)).thenReturn(user);
            when(fundService.getById(fundId)).thenReturn(fundResponse);

            AiRecommendation existing = AiRecommendation.builder()
                    .user(user)
                    .status(RecommendationStatus.PENDING)
                    .rationale("Valid Rationale")
                    .build();
            ReflectionTestUtils.setField(existing, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(existing, "createdAt", Instant.now().minus(1, ChronoUnit.HOURS));

            AIRecommendationResponse expectedResponse = mock(AIRecommendationResponse.class);

            when(aiRecommendationRepository.findLatestByFundAndUserAndStatus(fundId, email, RecommendationStatus.PENDING))
                    .thenReturn(Optional.of(existing));
            when(aiRecommendationMapper.toResponse(existing)).thenReturn(expectedResponse);

            AIRecommendationResponse result = aiRecommendationService.getPendingRecommendation(fundId, email);

            assertNotNull(result);
            assertSame(expectedResponse, result);
            verify(aiRecommendationRepository, never()).save(any());
            verifyNoInteractions(onnxModelService);
        }

        @Test
        @DisplayName("should expire existing recommendation and generate new one when expired")
        void shouldExpireAndGenerateNewWhenExpired() {
            FundProperties.Recommendation recProps = new FundProperties.Recommendation();
            recProps.setTtlHours(4);
            when(fundProperties.getRecommendation()).thenReturn(recProps);

            when(userService.findByEmail(email)).thenReturn(user);
            when(fundService.getById(fundId)).thenReturn(fundResponse);

            AiRecommendation expiredRec = AiRecommendation.builder()
                    .user(user)
                    .status(RecommendationStatus.PENDING)
                    .build();
            ReflectionTestUtils.setField(expiredRec, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(expiredRec, "createdAt", Instant.now().minus(6, ChronoUnit.HOURS));

            when(aiRecommendationRepository.findLatestByFundAndUserAndStatus(fundId, email, RecommendationStatus.PENDING))
                    .thenReturn(Optional.of(expiredRec));

            // Generate new mockings
            mockGenerateNewFlow();

            AIRecommendationResponse result = aiRecommendationService.getPendingRecommendation(fundId, email);

            assertNotNull(result);
            assertTrue(expiredRec.isDeleted());
            verify(aiRecommendationRepository).save(expiredRec); // saved expired
            verify(onnxModelService).getAction(any(float[].class));
        }

        @Test
        @DisplayName("should generate new recommendation when no pending recommendation exists")
        void shouldGenerateNewRecommendationWhenNoneExists() {
            when(userService.findByEmail(email)).thenReturn(user);
            when(fundService.getById(fundId)).thenReturn(fundResponse);
            when(aiRecommendationRepository.findLatestByFundAndUserAndStatus(fundId, email, RecommendationStatus.PENDING))
                    .thenReturn(Optional.empty());

            mockGenerateNewFlow();

            AIRecommendationResponse result = aiRecommendationService.getPendingRecommendation(fundId, email);

            assertNotNull(result);
            verify(onnxModelService).getAction(any(float[].class));
            verify(aiRecommendationRepository).save(any(AiRecommendation.class));
        }

        private void mockGenerateNewFlow() {
            when(fundDistributionService.getLatestByFundCode("TIE")).thenReturn(List.of());
            when(fundDistributionConverter.toWeightsMapFromResponses(any())).thenReturn(Map.of());
            when(marketDataRepository.findFirstByOrderByDataDateDesc()).thenReturn(Optional.empty());
            when(fundPeriodMetricService.getLatestByFundCode("TIE")).thenReturn(List.of());
            when(fundPriceDataRepository.findFirstByFundCodeOrderByDataDateDesc("TIE")).thenReturn(Optional.empty());

            FundModelInputRequest modelInput = FundModelInputRequest.builder()
                    .stockWeight(BigDecimal.valueOf(0.4))
                    .repoWeight(BigDecimal.valueOf(0.3))
                    .futureWeight(BigDecimal.valueOf(0.2))
                    .fundWeight(BigDecimal.valueOf(0.1))
                    .cdsSpreadBps(BigDecimal.valueOf(300))
                    .annualInflation(BigDecimal.valueOf(45))
                    .policyRate(BigDecimal.valueOf(50))
                    .build();
            when(aiRecommendationMapper.toModelInput(any(), any(), any(), any(), any())).thenReturn(modelInput);

            float[] predictedWeights = new float[]{0.45f, 0.25f, 0.20f, 0.10f};
            when(onnxModelService.getAction(any(float[].class))).thenReturn(predictedWeights);

            AiRecommendation newEntity = AiRecommendation.builder()
                    .user(user)
                    .status(RecommendationStatus.PENDING)
                    .rationale("Generated")
                    .build();
            when(aiRecommendationMapper.toEntity(fundResponse, user)).thenReturn(newEntity);
            when(aiRecommendationMapper.toWeightEntity(any(), any(), any())).thenReturn(new AiRecommendationWeight());

            FundStockBreakdownResponse breakdown = new FundStockBreakdownResponse(
                    "P10D", List.of(
                    new FundStockWeightResponse("THYAO", BigDecimal.valueOf(20)),
                    new FundStockWeightResponse("GARAN", BigDecimal.valueOf(30)),
                    new FundStockWeightResponse("Others", BigDecimal.valueOf(50))
            ));
            when(fundStockAllocationService.getBreakdownByFundCode("TIE", null)).thenReturn(breakdown);
            when(aiRecommendationMapper.toStockWeightEntity(any(), any(), any(), any())).thenReturn(new AiRecommendationStockWeight());

            when(aiRecommendationRepository.save(any(AiRecommendation.class))).thenAnswer(invocation -> invocation.getArgument(0));
            AIRecommendationResponse generatedResponse = mock(AIRecommendationResponse.class);
            when(aiRecommendationMapper.toResponse(newEntity)).thenReturn(generatedResponse);
        }
    }

    @Nested
    @DisplayName("submitRecommendationDecision")
    class SubmitRecommendationDecision {

        private UUID recommendationId;
        private AiRecommendation recommendation;

        @BeforeEach
        void setUp() {
            recommendationId = UUID.randomUUID();
            Fund fund = Fund.builder().code("TIE").name("Tacirler Portföy Değişken").build();
            recommendation = AiRecommendation.builder()
                    .user(user)
                    .fund(fund)
                    .status(RecommendationStatus.PENDING)
                    .rationale("Rationale")
                    .build();
            ReflectionTestUtils.setField(recommendation, "id", recommendationId);
        }

        @Test
        @DisplayName("should accept recommendation, attach simulation snapshot, and save")
        void shouldAcceptRecommendationSuccessfully() {
            when(aiRecommendationRepository.findById(recommendationId)).thenReturn(Optional.of(recommendation));

            assertDoesNotThrow(() -> aiRecommendationService.submitRecommendationDecision(
                    recommendationId, email, RecommendationStatus.ACCEPTED, "Accepted by manager"));

            assertEquals(RecommendationStatus.ACCEPTED, recommendation.getStatus());
            assertEquals("Accepted by manager", recommendation.getNote());
            verify(portfolioSimulationCalculationService).attachSnapshot(eq(recommendation), eq("TIE"), eq(30), any(), any());
            verify(aiRecommendationRepository).save(recommendation);
        }

        @Test
        @DisplayName("should reject recommendation and save without simulation snapshot")
        void shouldRejectRecommendationSuccessfully() {
            when(aiRecommendationRepository.findById(recommendationId)).thenReturn(Optional.of(recommendation));

            assertDoesNotThrow(() -> aiRecommendationService.submitRecommendationDecision(
                    recommendationId, email, RecommendationStatus.REJECTED, "Market too volatile"));

            assertEquals(RecommendationStatus.REJECTED, recommendation.getStatus());
            assertEquals("Market too volatile", recommendation.getNote());
            verifyNoInteractions(portfolioSimulationCalculationService);
            verify(aiRecommendationRepository).save(recommendation);
        }

        @Test
        @DisplayName("should throw AiRecommendationNotFoundException when recommendation not found")
        void shouldThrowWhenRecommendationNotFound() {
            when(aiRecommendationRepository.findById(recommendationId)).thenReturn(Optional.empty());

            assertThrows(AiRecommendationNotFoundException.class, () ->
                    aiRecommendationService.submitRecommendationDecision(
                            recommendationId, email, RecommendationStatus.ACCEPTED, "Note"));

            verify(aiRecommendationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UNAUTHORIZED_RECOMMENDATION when user email does not match recommendation owner")
        void shouldThrowWhenUserNotAuthorized() {
            when(aiRecommendationRepository.findById(recommendationId)).thenReturn(Optional.of(recommendation));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    aiRecommendationService.submitRecommendationDecision(
                            recommendationId, "different@test.com", RecommendationStatus.ACCEPTED, "Note"));

            assertEquals(FundErrorType.UNAUTHORIZED_RECOMMENDATION, ex.getErrorType());
            verify(aiRecommendationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw RECOMMENDATION_ALREADY_PROCESSED when recommendation status is not PENDING")
        void shouldThrowWhenAlreadyProcessed() {
            recommendation.setStatus(RecommendationStatus.ACCEPTED);
            when(aiRecommendationRepository.findById(recommendationId)).thenReturn(Optional.of(recommendation));

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    aiRecommendationService.submitRecommendationDecision(
                            recommendationId, email, RecommendationStatus.ACCEPTED, "Note"));

            assertEquals(FundErrorType.RECOMMENDATION_ALREADY_PROCESSED, ex.getErrorType());
            verify(aiRecommendationRepository, never()).save(any());
        }
    }
}
