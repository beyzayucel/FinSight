package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.converter.FundDistributionConverter;
import com.akademi.finsight.fund.dto.request.ManualScenarioRequest;
import com.akademi.finsight.fund.dto.response.FundStockBreakdownResponse;
import com.akademi.finsight.fund.dto.response.FundStockWeightResponse;
import com.akademi.finsight.fund.dto.response.ManualScenarioResponse;
import com.akademi.finsight.fund.entity.*;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.fund.mapper.ManualScenarioMapper;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.repository.ManualScenarioRepository;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.FundStockAllocationService;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManualScenarioServiceImpl Tests")
class ManualScenarioServiceImplTest {

    @Mock
    private FundRepository fundRepository;

    @Mock
    private ManualScenarioRepository manualScenarioRepository;

    @Mock
    private ScenarioValidationService validationService;

    @Mock
    private UserService userService;

    @Mock
    private ManualScenarioMapper manualScenarioMapper;

    @Mock
    private FundDistributionService fundDistributionService;

    @Mock
    private FundDistributionConverter fundDistributionConverter;

    @Mock
    private PortfolioSimulationCalculationService portfolioSimulationCalculationService;

    @Mock
    private FundStockAllocationService fundStockAllocationService;

    @InjectMocks
    private ManualScenarioServiceImpl manualScenarioService;

    private User user;
    private Fund fund;
    private UUID fundId;
    private final String email = "user@test.com";

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
    @DisplayName("applyManualScenario")
    class ApplyManualScenario {

        @Test
        @DisplayName("should validate and apply manual scenario with asset and stock weights successfully")
        void shouldApplyManualScenarioSuccessfully() {
            Map<AssetCategory, BigDecimal> targetWeights = Map.of(
                    AssetCategory.STOCK, BigDecimal.valueOf(50),
                    AssetCategory.REPO, BigDecimal.valueOf(50)
            );
            Map<String, BigDecimal> targetStockWeights = Map.of(
                    "THYAO", BigDecimal.valueOf(30),
                    "GARAN", BigDecimal.valueOf(20)
            );

            ManualScenarioRequest request = new ManualScenarioRequest();
            request.setFundId(fundId);
            request.setNote("My manual rebalance scenario");
            request.setWeights(targetWeights);
            request.setStockWeights(targetStockWeights);

            when(fundRepository.findById(fundId)).thenReturn(Optional.of(fund));
            when(userService.findByEmail(email)).thenReturn(user);

            Map<AssetCategory, BigDecimal> currentWeights = Map.of(
                    AssetCategory.STOCK, BigDecimal.valueOf(40),
                    AssetCategory.REPO, BigDecimal.valueOf(60)
            );
            when(fundDistributionService.getLatestByFundCode("TIE")).thenReturn(List.of());
            when(fundDistributionConverter.toPercentageWeightsMap(any())).thenReturn(currentWeights);

            FundStockBreakdownResponse breakdown = new FundStockBreakdownResponse("P10D", List.of(
                    new FundStockWeightResponse("THYAO", BigDecimal.valueOf(25)),
                    new FundStockWeightResponse("GARAN", BigDecimal.valueOf(15))
            ));
            when(fundStockAllocationService.getBreakdownByFundCode("TIE", null)).thenReturn(breakdown);

            ManualScenario entity = ManualScenario.builder()
                    .user(user)
                    .fund(fund)
                    .note("My manual rebalance scenario")
                    .build();
            when(manualScenarioMapper.toEntity(request, user, fund)).thenReturn(entity);
            when(manualScenarioMapper.toWeightEntity(any(), any(), any(), any())).thenReturn(new ManualScenarioWeight());
            when(manualScenarioMapper.toStockWeightEntity(any(), any(), any(), any())).thenReturn(new ManualScenarioStockWeight());

            assertDoesNotThrow(() -> manualScenarioService.applyManualScenario(email, request));

            verify(validationService).validate(eq(targetWeights), eq(currentWeights));
            verify(validationService).validateStockWeights(eq(targetStockWeights), anyMap());
            verify(portfolioSimulationCalculationService).attachSnapshot(eq(entity), eq("TIE"), eq(30), eq(targetWeights));
            verify(manualScenarioRepository).save(entity);
        }

        @Test
        @DisplayName("should throw FUND_NOT_FOUND when fund does not exist")
        void shouldThrowWhenFundNotFound() {
            ManualScenarioRequest request = new ManualScenarioRequest();
            request.setFundId(fundId);

            when(fundRepository.findById(fundId)).thenReturn(Optional.empty());

            FundValidationException ex = assertThrows(FundValidationException.class, () ->
                    manualScenarioService.applyManualScenario(email, request));

            assertEquals(FundErrorType.FUND_NOT_FOUND, ex.getErrorType());
            verify(manualScenarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getScenarioHistory")
    class GetScenarioHistory {

        @Test
        @DisplayName("should return scenario history mapped to responses")
        void shouldReturnScenarioHistory() {
            when(userService.findByEmail(email)).thenReturn(user);

            ManualScenario scenario = ManualScenario.builder()
                    .user(user)
                    .fund(fund)
                    .note("Historic note")
                    .build();
            ReflectionTestUtils.setField(scenario, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(scenario, "createdAt", Instant.now());

            ManualScenarioResponse expectedResponse = new ManualScenarioResponse(
                    scenario.getId(), fundId, "Historic note", scenario.getCreatedAt(),
                    List.of(), List.of(), null, null
            );

            when(manualScenarioRepository.findByUserIdAndFundIdOrderByCreatedAtDesc(user.getId(), fundId))
                    .thenReturn(List.of(scenario));
            when(manualScenarioMapper.toResponse(scenario)).thenReturn(expectedResponse);

            List<ManualScenarioResponse> actual = manualScenarioService.getScenarioHistory(email, fundId);

            assertNotNull(actual);
            assertEquals(1, actual.size());
            assertSame(expectedResponse, actual.get(0));
            verify(manualScenarioRepository).findByUserIdAndFundIdOrderByCreatedAtDesc(user.getId(), fundId);
        }
    }
}
