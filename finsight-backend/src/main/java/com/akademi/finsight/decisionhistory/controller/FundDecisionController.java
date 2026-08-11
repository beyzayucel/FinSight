package com.akademi.finsight.decisionhistory.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.decisionhistory.controller.api.FundDecisionApi;
import com.akademi.finsight.decisionhistory.dto.request.AttachStressTestRequest;
import com.akademi.finsight.decisionhistory.dto.response.DecisionRecordResponse;
import com.akademi.finsight.decisionhistory.service.DecisionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FundDecisionController extends BaseController implements FundDecisionApi {

    private final DecisionHistoryService decisionHistoryService;

    @Override
    public ResponseEntity<ApiStandardResponse<List<DecisionRecordResponse>>> getDecisionHistory(String email, UUID fundId) {
        return ok(decisionHistoryService.getHistory(email, fundId));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> attachStressTestResult(String email, AttachStressTestRequest request) {
        decisionHistoryService.attachStressTestResult(email, request);
        return ok();
    }
}
