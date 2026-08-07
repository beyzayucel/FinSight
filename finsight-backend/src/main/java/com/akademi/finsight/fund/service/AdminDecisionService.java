package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.response.AdminDecisionRecordResponse;
import com.akademi.finsight.fund.entity.DecisionType;

import java.util.List;
import java.util.UUID;

public interface AdminDecisionService {

    List<AdminDecisionRecordResponse> getDecisionReport(UUID userId, DecisionType type, Integer days);
}
