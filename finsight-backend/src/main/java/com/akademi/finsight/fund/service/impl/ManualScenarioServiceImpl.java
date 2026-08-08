package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.fund.converter.FundDistributionConverter;
import com.akademi.finsight.fund.dto.request.ManualScenarioRequest;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.dto.response.ManualScenarioResponse;
import com.akademi.finsight.fund.entity.*;
import com.akademi.finsight.fund.exception.FundErrorType;
import com.akademi.finsight.fund.exception.FundValidationException;
import com.akademi.finsight.fund.mapper.ManualScenarioMapper;
import com.akademi.finsight.fund.performancecomparison.service.PortfolioSimulationCalculationService;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.repository.ManualScenarioRepository;
import com.akademi.finsight.fund.service.FundDistributionService;
import com.akademi.finsight.fund.service.ManualScenarioService;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ManualScenarioServiceImpl implements ManualScenarioService {

    private final FundRepository fundRepository;
    private final ManualScenarioRepository manualScenarioRepository;
    private final ScenarioValidationService validationService;
    private final UserService userService;
    private final ManualScenarioMapper manualScenarioMapper;
    private final FundDistributionService fundDistributionService;
    private final FundDistributionConverter fundDistributionConverter;
    private final PortfolioSimulationCalculationService portfolioSimulationCalculationService;

    @Override
    @Transactional
    public void applyManualScenario(String email, ManualScenarioRequest manualScenario) {
        log.info("Starting to apply manual scenario for user email: {}, fundId: {}", MaskType.EMAIL.mask(email), manualScenario.getFundId());

        Fund fund = fundRepository.findById(manualScenario.getFundId())
                                  .orElseThrow(() -> new FundValidationException(FundErrorType.FUND_NOT_FOUND));

        User user = userService.findByEmail(email);

        Map<AssetCategory, BigDecimal> currentWeights = fetchCurrentWeightsFromDb(fund);

        Map<AssetCategory, BigDecimal> targetWeights = manualScenario.getWeights();

        log.debug("Validating weights. Target: {}, Current: {}", targetWeights, currentWeights);
        validationService.validate(targetWeights, currentWeights);

        ManualScenario scenario = manualScenarioMapper.toEntity(manualScenario, user, fund);

        for (Map.Entry<AssetCategory, BigDecimal> entry : targetWeights.entrySet()) {
            AssetCategory category = entry.getKey();
            BigDecimal targetWeight = entry.getValue();
            BigDecimal currentWeight = currentWeights.getOrDefault(category, BigDecimal.ZERO);

            ManualScenarioWeight weight = manualScenarioMapper.toWeightEntity(category, targetWeight, currentWeight, scenario);
            scenario.addWeight(weight);
        }

        portfolioSimulationCalculationService.attachSnapshot(scenario, fund.getCode(), 30, targetWeights);

        manualScenarioRepository.save(scenario);
        log.info("Manual scenario applied and saved successfully. Scenario ID: {}", scenario.getId());
    }

    @Override
    @Transactional
    public List<ManualScenarioResponse> getScenarioHistory(String email, UUID fundId) {
        log.info("Fetching manual scenario history for user email: {}, fundId: {}", MaskType.EMAIL.mask(email), fundId);

        User user = userService.findByEmail(email);

        List<ManualScenario> scenarios = manualScenarioRepository.findByUserIdAndFundIdOrderByCreatedAtDesc(user.getId(), fundId);

        return scenarios.stream()
                        .map(manualScenarioMapper::toResponse)
                        .toList();
    }

    private Map<AssetCategory, BigDecimal> fetchCurrentWeightsFromDb(Fund fund) {
        log.debug("Fetching latest fund distribution from DB for fund code: {}", fund.getCode());

        List<FundDistributionResponse> distributions = fundDistributionService.getLatestByFundCode(fund.getCode());

        Map<AssetCategory, BigDecimal> currentWeights = fundDistributionConverter.toPercentageWeightsMap(distributions);

        log.debug("Current weights map generated: {}", currentWeights);
        return currentWeights;
    }

}
