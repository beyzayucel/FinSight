package com.akademi.finsight.stresstest.service.impl;

import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.FundPeriodMetric;
import com.akademi.finsight.fund.entity.FundStockAllocation;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.repository.FundPeriodMetricRepository;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.repository.FundStockAllocationRepository;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.request.StressTestInferenceRequestDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.PortfolioResultDto;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.engine.StressTestCalculationEngine;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StressTestSimulationServiceImpl implements StressTestSimulationService {

    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final StressTestCalculationEngine calculationEngine;
    private final LLMCommentGenerator llmCommentGenerator;
    private final StressTestResultRepository stressTestResultRepository;
    private final StressTestResultMapper stressTestResultMapper;

    @Transactional
    @Override
    public StressTestInferenceResponseDto runSimulation(
            String userEmail,
            UUID fundId,
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

        StressTestResult entity = createStressTestResult(user, fund, simulationType);
        entity.addDetail(createDetail(PortfolioType.CURRENT_PORTFOLIO, currentPortfolio.initialValue(), currentResult));
        entity.addDetail(createDetail(PortfolioType.SIMULATION_PORTFOLIO, simulationPortfolio.initialValue(), simulationResult));
        entity.addDetail(createDetail(PortfolioType.BENCHMARK, benchmarkPortfolio.initialValue(), benchmarkResult));

        StressTestResult saved = stressTestResultRepository.save(entity);

        StressTestInferenceResponseDto response = stressTestResultMapper.toInferenceResponse(saved);
        String comment = llmCommentGenerator.generateComment(simulationType);
        return response.toBuilder().llmComment(comment).build();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StressTestInferenceResponseDto> getLatestSimulationResult(String userEmail, UUID fundId) {
        User user = findUser(userEmail);

        return stressTestResultRepository
                .findFirstByUserIdAndFundIdOrderByCreatedAtDesc(user.getId(), fundId)
                .map(result -> {
                    StressTestInferenceResponseDto response = stressTestResultMapper.toInferenceResponse(result);
                    String comment = llmCommentGenerator.generateComment(result.getSimulationType());
                    return response.toBuilder().llmComment(comment).build();
                });
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StressTestInferenceResponseDto> getSimulationResultByPeriod(
            String userEmail, UUID fundId, int daysAgo) {

        User user = findUser(userEmail);
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
        return calculationEngine.runInference(scenarioKey, portfolio);
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

    /**
     * Simülasyon Portföyü: şu an FE bu ekrandan alternatif ağırlık göndermiyor,
     * bu yüzden geçici olarak sabit bir "önerilen" ağırlık setine düşüyor.
     * FE ileride özel ağırlık göndermeye başlarsa customWeights parametresini
     * gerçek veriyle doldurman yeterli — imza zaten buna hazır.
     */
    private PortfolioDataDto buildSimulationPortfolioData(BigDecimal initialValue, Map<String, Float> customWeights) {
        Map<String, Float> weights = (customWeights != null && !customWeights.isEmpty())
                ? customWeights
                : Map.of("EQUITY", 0.40f, "BOND", 0.30f, "FX", 0.15f, "CASH", 0.15f);

        return createPortfolioData(initialValue, weights);
    }

    /**
     * Benchmark Portföyü: ayrı bir Benchmark entity'si olmadığı için sabit
     * referans ağırlıklarla oluşturuluyor — initialValue artık current
     * portföyle aynı, böylece kıyaslama tutarlı oluyor.
     */
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
}