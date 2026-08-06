package com.akademi.finsight.fund.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.fund.dto.request.FundPeriodMetricRequest;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.entity.FundPeriodMetric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface FundPeriodMetricMapper {

    @Mapping(target = "fund", ignore = true)
    @Mapping(target = "fetchedAt", ignore = true)
    FundPeriodMetric toEntity(FundPeriodMetricRequest request);

    @Mapping(target = "fund", ignore = true)
    @Mapping(target = "fetchedAt", ignore = true)
    void updateEntity(@MappingTarget FundPeriodMetric entity, FundPeriodMetricRequest request);

    @Mapping(target = "fundId", source = "fund.id")
    @Mapping(target = "benchmarkDiffBps", expression = "java(toBenchmarkDiffBps(entity))")
    FundPeriodMetricResponse toResponse(FundPeriodMetric entity);

    List<FundPeriodMetricResponse> toResponseList(List<FundPeriodMetric> entities);

    default BigDecimal toBenchmarkDiffBps(FundPeriodMetric entity) {
        if (entity.getCumulativeReturn() == null || entity.getBenchmarkReturn() == null) {
            return null;
        }
        return entity.getCumulativeReturn()
                .subtract(entity.getBenchmarkReturn())
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
