package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.response.FundDashboardResponse;

public interface FundDashboardService {

    FundDashboardResponse getDashboard(String fundCode);
}
