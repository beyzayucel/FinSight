package com.akademi.finsight.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        componentModel = "spring" ,
        unmappedTargetPolicy = ReportingPolicy.WARN)
public interface BaseMapperConfig {
}

