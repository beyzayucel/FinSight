package com.akademi.finsight.fund.performancecomparison.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.performancecomparison.controller.api.PerformanceComparisonApi;
import com.akademi.finsight.fund.performancecomparison.dto.response.PerformanceComparisonResponse;
import com.akademi.finsight.fund.performancecomparison.service.PerformanceComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PerformanceComparisonController extends BaseController implements PerformanceComparisonApi {

    private final PerformanceComparisonService performanceComparisonService;

    @Override
    public ResponseEntity<ApiStandardResponse<PerformanceComparisonResponse>> compare(String email,
                                                                                       String fundCode,
                                                                                       int analysisWindow) {
        return ok(performanceComparisonService.compare(email, fundCode, analysisWindow));
    }
}
