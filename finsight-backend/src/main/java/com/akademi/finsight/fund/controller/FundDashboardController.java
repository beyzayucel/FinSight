package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.controller.api.FundDashboardApi;
import com.akademi.finsight.fund.dto.response.FundDashboardResponse;
import com.akademi.finsight.fund.service.FundDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FundDashboardController extends BaseController implements FundDashboardApi {

    private final FundDashboardService fundDashboardService;

    @Override
    public ResponseEntity<ApiStandardResponse<FundDashboardResponse>> getDashboard(String fundCode) {
        return ok(fundDashboardService.getDashboard(fundCode));
    }
}
