package com.akademi.finsight.fund.performancecomparison.service;

import com.akademi.finsight.fund.entity.AssetCategory;
import com.akademi.finsight.fund.entity.MetricsHolder;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse.PortfolioCurve;

import java.math.BigDecimal;
import java.util.Map;

public interface PortfolioSimulationCalculationService {

    PortfolioCurve calculateSimulation(String fundCode, int analysisWindow,
                                       Map<AssetCategory, BigDecimal> simulationWeights,
                                       Map<String, BigDecimal> simulationStockWeights);

    void attachSnapshot(MetricsHolder holder, String fundCode, int analysisWindow,
                        Map<AssetCategory, BigDecimal> weights, Map<String, BigDecimal> stockWeights);
}