package com.akademi.finsight.fund.performancecomparison.service;

import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse;

public interface PerformanceComparisonService {

    PerformanceComparisonResponse compare(String fundCode, int analysisWindow);
}
