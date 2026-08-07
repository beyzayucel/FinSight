package com.akademi.finsight.fund.service;

import java.util.List;
import java.util.UUID;

import com.akademi.finsight.fund.dto.request.AttachMetricsRequest;
import com.akademi.finsight.fund.dto.request.AttachStressTestRequest;
import com.akademi.finsight.fund.dto.response.DecisionRecordResponse;

public interface DecisionHistoryService {

    List<DecisionRecordResponse> getHistory(String email, UUID fundId);

    void attachMetrics(String email, AttachMetricsRequest request);

    void attachStressTestResult(String email, AttachStressTestRequest request);
}
