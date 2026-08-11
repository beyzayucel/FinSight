package com.akademi.finsight.decisionhistory.service;

import com.akademi.finsight.decisionhistory.dto.response.AdminDecisionRecordResponse;
import com.akademi.finsight.decisionhistory.dto.response.DecisionRecordResponse;
import com.akademi.finsight.decisionhistory.entity.DecisionType;

import java.util.List;
import java.util.UUID;

public interface AdminDecisionService {

    List<AdminDecisionRecordResponse> getDecisionReport(UUID userId, DecisionType type, Integer days);

    DecisionRecordResponse getDecisionDetail(UUID decisionId, DecisionType type);
}
