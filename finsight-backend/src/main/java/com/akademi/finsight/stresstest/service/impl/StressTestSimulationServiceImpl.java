package com.akademi.finsight.stresstest.service.impl;

import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.request.SaveStressTestDecisionRequestDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.engine.StressTestStrategyFactory;
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import com.akademi.finsight.stresstest.enums.PortfolioType;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.entity.StressTestResultDetail;
import com.akademi.finsight.stresstest.llm.LLMCommentGenerator;
import com.akademi.finsight.stresstest.mapper.StressTestResultMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StressTestSimulationServiceImpl implements StressTestSimulationService {

    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final StressTestStrategyFactory strategyFactory;
    private final LLMCommentGenerator llmCommentGenerator;
    private final StressTestResultRepository stressTestResultRepository;
    private final StressTestResultMapper stressTestResultMapper;

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

        StressTestResult stressTestResult  = createStressTestResult(user, fund, simulationType);
        StressTestResultDetail currentDetail = createDetail(
                PortfolioType.CURRENT_PORTFOLIO,
                currentPortfolio.initialValue(),
                currentResult
        );
        stressTestResult.addDetail(currentDetail);

        StressTestResultDetail simulationDetail = createDetail(
                PortfolioType.SIMULATION_PORTFOLIO,
                simulationPortfolio.initialValue(),
                simulationResult
        );
        stressTestResult.addDetail(simulationDetail);

        StressTestResultDetail benchmarkDetail = createDetail(
                PortfolioType.BENCHMARK,
                benchmarkPortfolio.initialValue(),
                benchmarkResult
        );
        stressTestResult.addDetail(benchmarkDetail);

        StressTestInferenceResponseDto response = stressTestResultMapper.toInferenceResponse(stressTestResult);
        String comment = llmCommentGenerator.generateComment(simulationType);
        return response.toBuilder().llmComment(comment).build();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StressTestInferenceResponseDto> getLatestSimulationResult(String userEmail, String fundId) {
        User user = findUser(userEmail);

        return stressTestResultRepository
                .findFirstByUserIdAndFundIdOrderByCreatedAtDesc(user.getId().toString(), fundId)
                .map(result -> {
                    StressTestInferenceResponseDto response = stressTestResultMapper.toInferenceResponse(result);
                    String comment = llmCommentGenerator.generateComment(result.getSimulationType());
                    return response.toBuilder().llmComment(comment).build();
                });
    }


    @Transactional(readOnly = true)
    @Override
    public Optional<StressTestInferenceResponseDto> getSimulationResultByPeriod(
            String userEmail, String fundId, String lookbackDays) {

        User user = findUser(userEmail);

        // "90d", "3M" gibi periyot string'ini güne çeviren küçük bir helper
        int daysAgo = parsePeriodToDays(lookbackDays); // Örn: "3M" veya "90" -> 90 gün

        LocalDateTime targetDateTime = LocalDate.now().minusDays(daysAgo).atTime(LocalTime.MAX);

        return stressTestResultRepository
                .findFirstByUserIdAndFundIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
                        user.getId(), fundId, targetDateTime)
                .map(result -> {
                    StressTestInferenceResponseDto response = stressTestResultMapper.toInferenceResponse(result);
                    String comment = llmCommentGenerator.generateComment(result.getSimulationType());
                    return response.toBuilder().llmComment(comment).build();
                });
    }

    @Transactional
    @Override
    public void saveDecisionRecord(String userEmail, SaveStressTestDecisionRequestDto requestDto) {
        User user = findUser(userEmail);
        Fund fund = findFund(requestDto.fundId());

        // Var olan DTO'yu kullanarak simülasyon koşturabilir veya var olan sonucu geçmişe kaydedebilirsin:
        StressTestResult stressTestResult  = createStressTestResult(user, fund, requestDto.scenarioKey());

        // DB'ye kaydetme işlemi:
        stressTestResultRepository.save(stressTestResult );
        log.info("Stress test decision saved successfully for user: {} and fund: {}", userEmail, fund.getCode());
    }

    private int parsePeriodToDays(String period) {
        if (period == null) return 0;
        return switch (period.toUpperCase()) {
            case "1M", "30D" -> 30;
            case "3M", "90D" -> 90;
            case "6M", "180D" -> 180;
            case "1Y", "365D" -> 365;
            default -> Integer.parseInt(period); // Sayı olarak geçildiyse
        };
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

    private Fund findFund(UUID fundId) {
        return fundRepository.findById(fundId)
                .orElseThrow(FundNotFoundException::new);
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

    // findFund metodunda koda göre fon bulma:
    private Fund findFund(String fundIdOrCode) {
        // Eğer fon kodu ("TIE") geliyorsa koda göre, UUID geliyorsa id'ye göre ara:
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