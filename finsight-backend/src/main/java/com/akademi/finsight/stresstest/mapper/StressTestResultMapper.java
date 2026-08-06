package com.akademi.finsight.stresstest.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.stresstest.dto.request.StressTestInferenceRequestDto;
import com.akademi.finsight.stresstest.dto.response.PortfolioResultDto;
import com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto;
import com.akademi.finsight.stresstest.entity.PortfolioType;
import com.akademi.finsight.stresstest.entity.SimulationType;
import com.akademi.finsight.stresstest.entity.StressTestResult;
import com.akademi.finsight.stresstest.entity.StressTestResultDetail;
import com.akademi.finsight.stresstest.exception.StressTestErrorType;
import com.akademi.finsight.stresstest.exception.StressTestException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface StressTestResultMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "fund", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "simulationType", expression = "java(mapSimulationType(request.scenarioKey()))")
    StressTestResult toEntity(StressTestInferenceRequestDto request);

    @Mapping(target = "scenarioKey", expression = "java(entity.getSimulationType() != null ? entity.getSimulationType().name() : null)")
    @Mapping(target = "currentPortfolioResult", expression = "java(findPortfolioResult(entity, PortfolioType.CURRENT_PORTFOLIO))")
    @Mapping(target = "simulationPortfolioResult", expression = "java(findPortfolioResult(entity, PortfolioType.SIMULATION_PORTFOLIO))")
    @Mapping(target = "benchmarkPortfolioResult", expression = "java(findPortfolioResult(entity, PortfolioType.BENCHMARK))")
    StressTestInferenceResponseDto toInferenceResponse(StressTestResult entity);

    PortfolioResultDto toPortfolioResultDto(StressTestResultDetail detail);

    List<StressTestInferenceResponseDto> toInferenceResponseList(List<StressTestResult> entities);

    @AfterMapping
    default void linkDetails(@MappingTarget StressTestResult result) {
        if (result.getDetails() != null) {
            result.getDetails().forEach(detail -> detail.setStressTestResult(result));
        }
    }

    default SimulationType mapSimulationType(String scenarioKey) {
        if (scenarioKey == null || scenarioKey.isBlank()) {
            return null;
        }
        try {
            return SimulationType.valueOf(scenarioKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StressTestException(StressTestErrorType.INVALID_SIMULATION_TYPE);
        }
    }

    default PortfolioResultDto findPortfolioResult(StressTestResult entity, PortfolioType portfolioType) {
        if (entity == null || entity.getDetails() == null) {
            return null;
        }
        return entity.getDetails().stream()
                .filter(detail -> detail.getPortfolioType() == portfolioType)
                .findFirst()
                .map(this::toPortfolioResultDto)
                .orElse(null);
    }
}
