package com.akademi.finsight.fund.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.fund.dto.request.FundRequest;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.entity.Fund;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface FundMapper {

    Fund toEntity(FundRequest request);

    void updateEntity(@MappingTarget Fund entity, FundRequest request);

    FundResponse toResponse(Fund entity);

    List<FundResponse> toResponseList(List<Fund> entities);
}
