package com.akademi.finsight.fund.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.fund.dto.request.FundDistributionRequest;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.entity.FundDistribution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface FundDistributionMapper {

    @Mapping(target = "fund", ignore = true)
    FundDistribution toEntity(FundDistributionRequest request);

    @Mapping(target = "fund", ignore = true)
    void updateEntity(@MappingTarget FundDistribution entity, FundDistributionRequest request);

    @Mapping(target = "fundId", source = "fund.id")
    FundDistributionResponse toResponse(FundDistribution entity);

    List<FundDistributionResponse> toResponseList(List<FundDistribution> entities);
}
