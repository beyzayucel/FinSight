package com.akademi.finsight.fund.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.fund.dto.request.FundStockAllocationRequest;
import com.akademi.finsight.fund.dto.response.FundStockAllocationResponse;
import com.akademi.finsight.fund.entity.FundStockAllocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface FundStockAllocationMapper {

    @Mapping(target = "fund", ignore = true)
    FundStockAllocation toEntity(FundStockAllocationRequest request);

    @Mapping(target = "fund", ignore = true)
    void updateEntity(@MappingTarget FundStockAllocation entity, FundStockAllocationRequest request);

    @Mapping(target = "fundId", source = "fund.id")
    FundStockAllocationResponse toResponse(FundStockAllocation entity);

    List<FundStockAllocationResponse> toResponseList(List<FundStockAllocation> entities);
}
