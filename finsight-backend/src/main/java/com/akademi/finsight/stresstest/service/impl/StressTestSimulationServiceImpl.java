package com.akademi.finsight.stresstest.service.impl;

import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.stresstest.dto.request.PortfolioDataDto;
import com.akademi.finsight.stresstest.dto.request.StressTestInferenceRequestDto;
import com.akademi.finsight.stresstest.dto.response.ModelInferenceResult;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.enums.PortfolioType;
import com.akademi.finsight.stresstest.enums.SimulationType;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.entity.StressTestResultDetail;
import com.akademi.finsight.stresstest.mapper.StressTestResultMapper;
import com.akademi.finsight.stresstest.repository.StressTestResultRepository;
import com.akademi.finsight.stresstest.service.OnnxModelRunner;
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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StressTestSimulationServiceImpl implements StressTestSimulationService {

    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final OnnxModelRunner onnxModelRunner;
    private final StressTestResultRepository stressTestResultRepository;
    private final StressTestResultMapper stressTestResultMapper;


    @Transactional
    @Override
    public StressTestInferenceResponseDto runSimulation(String userEmail, UUID fundId, SimulationType simulationType) {
        User user = findUser(userEmail);
        Fund fund = findFund(fundId);

        StressTestInferenceRequestDto request = createInferenceRequest(user.getId(), fund.getId(), simulationType);

        StressTestResult result = executeSimulation(user, fund, simulationType, request);

        return stressTestResultMapper.toInferenceResponse(result);
    }

    @Transactional
    @Override
    public Optional<StressTestInferenceResponseDto> getLatestSimulationResult(String userEmail, UUID fundId){
        User user = findUser(userEmail);

        return stressTestResultRepository
                .findFirstByUserIdAndFundIdOrderByCreatedAtDesc(user.getId(), fundId)
                .map(stressTestResultMapper::toInferenceResponse);

    }



    private StressTestInferenceRequestDto createInferenceRequest( UUID userId, UUID fundId, SimulationType simulationType){

        PortfolioDataDto current = buildCurrentPortfolioData(fundId);
        PortfolioDataDto simulation = buildSimulationPortfolioData(userId, fundId);
        PortfolioDataDto benchmark = buildBenchmarkPortfolioData(current.initialValue());

        return StressTestInferenceRequestDto.builder()
                .scenarioKey(simulationType.name())
                .currentPortfolio(current)
                .simulationPortfolio(simulation)
                .benchmarkPortfolio(benchmark)
                .build();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new UserException(UserErrorType.USER_NOT_FOUND));
    }

    private Fund findFund(UUID fundId) {
        return fundRepository.findById(fundId)
                .orElseThrow(FundNotFoundException::new);
    }

    private StressTestResult executeSimulation(
            User user,
            Fund fund,
            SimulationType simulationType,
            StressTestInferenceRequestDto request) {

        ModelInferenceResult current = runModel(request.scenarioKey(), request.currentPortfolio());
        ModelInferenceResult simulation = runModel(request.scenarioKey(), request.simulationPortfolio());
        ModelInferenceResult benchmark = runModel(request.scenarioKey(), request.benchmarkPortfolio());

        StressTestResult result = createStressTestResult(user, fund, simulationType);

        result.addDetail(createDetail(
                PortfolioType.CURRENT_PORTFOLIO,
                request.currentPortfolio().initialValue(),
                current));

        result.addDetail(createDetail(
                PortfolioType.SIMULATION_PORTFOLIO,
                request.simulationPortfolio().initialValue(),
                simulation));

        result.addDetail(createDetail(
                PortfolioType.BENCHMARK,
                request.benchmarkPortfolio().initialValue(),
                benchmark));

        return stressTestResultRepository.save(result);
    }

    private ModelInferenceResult runModel(
            String scenarioKey,
            PortfolioDataDto portfolio) {

        return onnxModelRunner.runInference(
                scenarioKey,
                portfolio
        );
    }

    private StressTestResult createStressTestResult(
            User user,
            Fund fund,
            SimulationType simulationType) {

        return StressTestResult.builder()
                .user(user)
                .fund(fund)
                .simulationType(simulationType)
                .build();
    }


    private PortfolioDataDto buildCurrentPortfolioData(UUID fundId) {
        // Toplam portföy değerini bilgileri

        return null;
    }


    private PortfolioDataDto buildSimulationPortfolioData(UUID userId, UUID fundId) {
        // Kullanıcının seçtiği senaryoya göre simülasyon portföy hesaplanacak

        return null;
    }

    private PortfolioDataDto buildBenchmarkPortfolioData(BigDecimal currentInitialValue) {
        // Benchmark serviceden çekilecek
        return null;
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
