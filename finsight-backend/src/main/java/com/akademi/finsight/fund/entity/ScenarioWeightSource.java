package com.akademi.finsight.fund.entity;

import com.akademi.finsight.fund.decision.entity.AssetCategory;

import java.math.BigDecimal;
import java.util.Map;

public interface ScenarioWeightSource {

    Map<AssetCategory, BigDecimal> getSimulationWeights();

    /** Top-N hisse icin kullanicinin/onerinin hedef agirliklari (assetCode -> weight, sleeve-relative, 0-100). */
    Map<String, BigDecimal> getSimulationStockWeights();
}
