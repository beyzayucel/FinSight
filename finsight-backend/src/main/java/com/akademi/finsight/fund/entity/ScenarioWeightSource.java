package com.akademi.finsight.fund.entity;

import java.math.BigDecimal;
import java.util.Map;

public interface ScenarioWeightSource {

    Map<AssetCategory, BigDecimal> getSimulationWeights();
}
