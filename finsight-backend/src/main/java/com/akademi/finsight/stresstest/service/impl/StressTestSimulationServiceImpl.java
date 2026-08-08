package com.akademi.finsight.stresstest.service.impl;

import com.akademi.finsight.fund.dto.request.AttachStressTestRequest;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.service.DecisionHistoryService;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.request.SaveStressTestDecisionRequestDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.engine.StressTestStrategyFactory;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.entity.StressTestResultDetail;
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import com.akademi.finsight.stresstest.enums.PortfolioType;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import com.akademi.finsight.stresstest.mapper.StressTestResponseAssembler;
import com.akademi.finsight.stresstest.repository.StressTestResultRepository;
import com.akademi.finsight.stresstest.service.StressTestSimulationService;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.UserErrorType;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StressTestSimulationServiceImpl implements StressTestSimulationService {

    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final StressTestStrategyFactory strategyFactory;
    private final StressTestResultRepository stressTestResultRepository;
    private final StressTestResponseAssembler stressTestResponseAssembler;
    private final DecisionHistoryService decisionHistoryService;

    @Transactional
    @Override
    public StressTestInferenceResponseDto runSimulation(
            String userEmail,
            String fundId,
            SimulationType simulationType,
            PortfolioDataDto currentPortfolio) {

        validatePortfolio(currentPortfolio);

        User user = findUser(userEmail);
        Fund fund = findFund(fundId);

        PortfolioDataDto simulationPortfolio =
                buildSimulationPortfolioData(currentPortfolio.initialValue(), null);
        PortfolioDataDto benchmarkPortfolio =
                buildBenchmarkPortfolioData(currentPortfolio.initialValue());

        ModelInferenceResult currentResult = runModel(simulationType.name(), currentPortfolio);
        ModelInferenceResult simulationResult = runModel(simulationType.name(), simulationPortfolio);
        ModelInferenceResult benchmarkResult = runModel(simulationType.name(), benchmarkPortfolio);

        StressTestResult stressTestResult = createStressTestResult(user, fund, simulationType);
        stressTestResult.addDetail(createDetail(
                PortfolioType.CURRENT_PORTFOLIO,
                currentPortfolio.initialValue(),
                currentResult
        ));
        stressTestResult.addDetail(createDetail(
                PortfolioType.SIMULATION_PORTFOLIO,
                simulationPortfolio.initialValue(),
                simulationResult
        ));
        stressTestResult.addDetail(createDetail(
                PortfolioType.BENCHMARK,
                benchmarkPortfolio.initialValue(),
                benchmarkResult
        ));

        // Sonuç burada kalıcı hale gelir: hem /latest ve /period uçları bunu okur, hem de
        // "Karar Geçmişine Kaydet" akışı dönen id ile bu satırı karara iliştirir.
        StressTestResult saved = stressTestResultRepository.save(stressTestResult);
        log.info("Stress test simulation persisted. resultId: {}, fundCode: {}, simulationType: {}",
                saved.getId(), fund.getCode(), simulationType);

        return stressTestResponseAssembler.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StressTestInferenceResponseDto> getLatestSimulationResult(String userEmail, String fundId) {
        User user = findUser(userEmail);
        Fund fund = findFund(fundId);

        return stressTestResultRepository
                .findFirstByUserIdAndFundIdOrderByCreatedAtDesc(user.getId(), fund.getId())
                .map(stressTestResponseAssembler::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StressTestInferenceResponseDto> getSimulationResultByPeriod(
            String userEmail, String fundId, String lookbackDays) {

        User user = findUser(userEmail);
        Fund fund = findFund(fundId);

        int daysAgo = parsePeriodToDays(lookbackDays);
        Instant upperBound = LocalDate.now()
                                      .minusDays(daysAgo)
                                      .atTime(LocalTime.MAX)
                                      .toInstant(ZoneOffset.UTC);

        return stressTestResultRepository
                .findFirstByUserIdAndFundIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
                        user.getId(), fund.getId(), upperBound)
                .map(stressTestResponseAssembler::toResponse);
    }

    /**
     * "Karar Geçmişine Kaydet" — sonucu yeniden hesaplamaz, /run sırasında kaydedilmiş satırı
     * o fondaki en güncel karara iliştirir. FK'yı yazan tek yer DecisionHistoryService'tir;
     * sahiplik kontrolü ve "iliştirilecek karar yok" durumu da orada ele alınır.
     */
    @Transactional
    @Override
    public void saveDecisionRecord(String userEmail, SaveStressTestDecisionRequestDto requestDto) {
        decisionHistoryService.attachStressTestResult(
                userEmail,
                new AttachStressTestRequest(requestDto.fundId(), requestDto.stressTestResultId())
        );
    }

    private int parsePeriodToDays(String period) {
        if (period == null || period.isBlank()) {
            throw new StressTestException(StressTestErrorType.INVALID_ANALYSIS_PERIOD);
        }
        return switch (period.toUpperCase()) {
            case "1M", "30D" -> 30;
            case "3M", "90D" -> 90;
            case "6M", "180D" -> 180;
            case "1Y", "365D" -> 365;
            default -> parseDayCount(period);
        };
    }

    private int parseDayCount(String period) {
        try {
            int days = Integer.parseInt(period.trim());
            if (days < 0) {
                throw new StressTestException(StressTestErrorType.INVALID_ANALYSIS_PERIOD);
            }
            return days;
        } catch (NumberFormatException e) {
            log.warn("Invalid analysis period requested: {}", period);
            throw new StressTestException(StressTestErrorType.INVALID_ANALYSIS_PERIOD);
        }
    }

    private void validatePortfolio(PortfolioDataDto portfolio) {
        if (portfolio == null || portfolio.assetWeights() == null || portfolio.assetWeights().isEmpty()) {
            throw new IllegalArgumentException("Portfolio verisi (initialValue + assetWeights) zorunludur.");
        }
        if (portfolio.initialValue() == null || portfolio.initialValue().signum() <= 0) {
            throw new IllegalArgumentException("initialValue pozitif olmalıdır.");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
    }

    private ModelInferenceResult runModel(String scenarioKey, PortfolioDataDto portfolio) {
        return runModel(scenarioKey, portfolio, ExecutionStrategyType.MANUAL_RULE);
    }

    private ModelInferenceResult runModel(String scenarioKey, PortfolioDataDto portfolio, ExecutionStrategyType strategyType) {
        ExecutionStrategyType activeStrategy = (strategyType != null) ? strategyType : ExecutionStrategyType.MANUAL_RULE;
        return strategyFactory.getEngine(activeStrategy).runInference(scenarioKey, portfolio);
    }

    private StressTestResult createStressTestResult(User user, Fund fund, SimulationType simulationType) {
        return StressTestResult.builder()
                .user(user)
                .fund(fund)
                .simulationType(simulationType)
                .build();
    }

    private PortfolioDataDto createPortfolioData(BigDecimal initialValue, Map<String, Float> weights) {
        return PortfolioDataDto.builder()
                .initialValue(initialValue)
                .assetWeights(weights)
                .build();
    }

    private PortfolioDataDto buildSimulationPortfolioData(BigDecimal initialValue, Map<String, Float> customWeights) {
        Map<String, Float> weights = (customWeights != null && !customWeights.isEmpty())
                ? customWeights
                : Map.of("EQUITY", 0.40f, "BOND", 0.30f, "FX", 0.15f, "CASH", 0.15f);

        return createPortfolioData(initialValue, weights);
    }

    private PortfolioDataDto buildBenchmarkPortfolioData(BigDecimal initialValue) {
        Map<String, Float> benchmarkWeights = Map.of(
                "EQUITY", 0.50f,
                "BOND", 0.25f,
                "FX", 0.15f,
                "CASH", 0.10f
        );

        return createPortfolioData(initialValue, benchmarkWeights);
    }

    private StressTestResultDetail createDetail(PortfolioType type, BigDecimal initialVal, ModelInferenceResult result) {
        return StressTestResultDetail.builder()
                .portfolioType(type)
                .initialValue(initialVal)
                .expectedImpactRate(result.expectedImpactRate())
                .postShockValue(result.postShockValue())
                .build();
    }

    // Fon kodu ("TIE") ya da UUID ile arama — Stres Testi ekranı ikisini de gönderebiliyor.
    private Fund findFund(String fundIdOrCode) {
        return fundRepository.findByCode(fundIdOrCode)
                .orElseGet(() -> {
                    try {
                        return fundRepository.findById(UUID.fromString(fundIdOrCode))
                                .orElseThrow(FundNotFoundException::new);
                    } catch (IllegalArgumentException e) {
                        throw new FundNotFoundException();
                    }
                });
    }
}
