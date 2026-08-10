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
import com.akademi.finsight.stresstest.enums.ExecutionStrategyType;
import com.akademi.finsight.stresstest.enums.PortfolioType;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import com.akademi.finsight.stresstest.mapper.StressTestMapper;
import com.akademi.finsight.stresstest.repository.StressTestResultRepository;
import com.akademi.finsight.stresstest.service.StressTestSimulationService;
import com.akademi.finsight.stresstest.service.helper.PortfolioDataBuilder;
import com.akademi.finsight.stresstest.util.StressTestPeriodParser;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.UserErrorType;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;



@Slf4j
@Service
@RequiredArgsConstructor
public class StressTestSimulationServiceImpl implements StressTestSimulationService {

    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final StressTestStrategyFactory strategyFactory;
    private final StressTestResultRepository stressTestResultRepository;
    private final DecisionHistoryService decisionHistoryService;

    private final PortfolioDataBuilder portfolioDataBuilder;
    private final StressTestPeriodParser periodParser;
    private final StressTestMapper stressTestMapper;

    @Transactional
    @Override
    public StressTestInferenceResponseDto runSimulation(
            String userEmail,
            String fundId,
            SimulationType simulationType,
            PortfolioDataDto currentPortfolio,
            int analysisWindow) {

        validatePortfolio(currentPortfolio);

        User user = findUser(userEmail);
        Fund fund = findFund(fundId);

        PortfolioDataDto simulationPortfolio =
                portfolioDataBuilder.buildSimulationPortfolio(userEmail, fund.getCode(), analysisWindow, currentPortfolio.initialValue());
        PortfolioDataDto benchmarkPortfolio =
                portfolioDataBuilder.buildBenchmarkPortfolio(fund.getCode(), analysisWindow, currentPortfolio.initialValue());

        ModelInferenceResult currentResult = runModel(simulationType.name(), currentPortfolio);
        ModelInferenceResult simulationResult = runModel(simulationType.name(), simulationPortfolio);
        ModelInferenceResult benchmarkResult = runModel(simulationType.name(), benchmarkPortfolio);

        StressTestResult stressTestResult = stressTestMapper.createStressTestResult(user, fund, simulationType);

        stressTestResult.addDetail(stressTestMapper.createDetail(
                PortfolioType.CURRENT_PORTFOLIO, currentPortfolio.initialValue(), currentResult));
        stressTestResult.addDetail(stressTestMapper.createDetail(
                PortfolioType.SIMULATION_PORTFOLIO, simulationPortfolio.initialValue(), simulationResult));
        stressTestResult.addDetail(stressTestMapper.createDetail(
                PortfolioType.BENCHMARK, benchmarkPortfolio.initialValue(), benchmarkResult));

        StressTestResult saved = stressTestResultRepository.save(stressTestResult);
        log.info("Stress test simulation persisted. resultId: {}, fundCode: {}, simulationType: {}",
                saved.getId(), fund.getCode(), simulationType);

        return stressTestMapper.toInferenceResponseDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StressTestInferenceResponseDto> getLatestSimulationResult(String userEmail, String fundId) {
        User user = findUser(userEmail);
        Fund fund = findFund(fundId);

        return stressTestResultRepository
                .findFirstByUserIdAndFundIdOrderByCreatedAtDesc(user.getId(), fund.getId())
                .map(stressTestMapper::toInferenceResponseDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StressTestInferenceResponseDto> getSimulationResultByPeriod(
            String userEmail, String fundId, String lookbackDays) {

        User user = findUser(userEmail);
        Fund fund = findFund(fundId);

        int daysAgo = periodParser.parseToDays(lookbackDays);

        Instant lowerBound = LocalDate.now().minusDays(daysAgo).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant upperBound = LocalDate.now().atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);

        return stressTestResultRepository
                .findFirstByPeriod(user.getId(), fund.getId(), lowerBound, upperBound)
                .map(stressTestMapper::toInferenceResponseDto);
    }

    @Transactional
    @Override
    public void saveDecisionRecord(String userEmail, SaveStressTestDecisionRequestDto requestDto) {
        UUID fundUuid = UUID.fromString(String.valueOf(requestDto.fundId()));
        UUID resultUuid = UUID.fromString(String.valueOf(requestDto.stressTestResultId()));

        if (!stressTestResultRepository.existsById(resultUuid)) {
            throw new StressTestException(StressTestErrorType.RESULT_NOT_FOUND);
        }

        decisionHistoryService.attachStressTestResult(
                userEmail,
                new AttachStressTestRequest(fundUuid, resultUuid)
        );
    }

    private ModelInferenceResult runModel(String scenarioKey, PortfolioDataDto portfolio) {
        return runModel(scenarioKey, portfolio, ExecutionStrategyType.MANUAL_RULE);
    }

    private ModelInferenceResult runModel(String scenarioKey, PortfolioDataDto portfolio, ExecutionStrategyType strategyType) {
        ExecutionStrategyType activeStrategy = (strategyType != null) ? strategyType : ExecutionStrategyType.MANUAL_RULE;
        try {
            return strategyFactory.getEngine(activeStrategy).runInference(scenarioKey, portfolio);
        } catch (StressTestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Model inference execution failed for scenarioKey: {}", scenarioKey, ex);
            throw new StressTestException(StressTestErrorType.MODEL_INFERENCE_ERROR, ex);
        }
    }

    private void validatePortfolio(PortfolioDataDto portfolio) {
        if (portfolio == null || portfolio.assetWeights() == null || portfolio.assetWeights().isEmpty()) {
            throw new StressTestException(StressTestErrorType.INVALID_INPUT);
        }
        if (portfolio.initialValue() == null || portfolio.initialValue().signum() <= 0) {
            throw new StressTestException(StressTestErrorType.INVALID_INPUT);
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
    }

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