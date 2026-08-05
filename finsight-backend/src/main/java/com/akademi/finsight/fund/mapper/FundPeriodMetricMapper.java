package com.akademi.finsight.fund.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.fund.dto.request.FundPeriodMetricRequest;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.entity.FundPeriodMetric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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
    FundPeriodMetricResponse toResponse(FundPeriodMetric entity);

    List<FundPeriodMetricResponse> toResponseList(List<FundPeriodMetric> entities);
}
