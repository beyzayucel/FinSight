package com.akademi.finsight.decisionhistory.service;

import java.util.List;
import java.util.UUID;

import com.akademi.finsight.decisionhistory.dto.request.AttachStressTestRequest;
import com.akademi.finsight.decisionhistory.dto.response.DecisionRecordResponse;

public interface DecisionHistoryService {

    List<DecisionRecordResponse> getHistory(String email, UUID fundId);

    void attachStressTestResult(String email, AttachStressTestRequest request);
}
