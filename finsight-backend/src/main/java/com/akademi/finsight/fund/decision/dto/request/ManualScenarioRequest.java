package com.akademi.finsight.fund.decision.dto.request;

import com.akademi.finsight.fund.decision.entity.AssetCategory;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ManualScenarioRequest {

    @NotNull
    private UUID fundId;

    private String note;

    @NotEmpty
    private Map<AssetCategory, BigDecimal> weights;

    private Map<String, BigDecimal> stockWeights;
}