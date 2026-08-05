package com.akademi.finsight.fund.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.fund.dto.request.ManualScenarioRequest;
import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.ManualScenario;
import com.akademi.finsight.fund.entity.ManualScenarioWeight;
import com.akademi.finsight.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(config = BaseMapperConfig.class)
public interface ManualScenarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "weights", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "fund", source = "fund")
    ManualScenario toEntity(ManualScenarioRequest request, User user, Fund fund);

    ManualScenarioWeight toWeightEntity(AssetCategory category, BigDecimal targetWeight, BigDecimal currentWeight, ManualScenario scenario);
}