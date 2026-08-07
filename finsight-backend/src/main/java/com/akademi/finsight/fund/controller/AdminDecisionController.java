package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.controller.api.AdminDecisionApi;
import com.akademi.finsight.fund.dto.response.AdminDecisionRecordResponse;
import com.akademi.finsight.fund.entity.DecisionType;
import com.akademi.finsight.fund.service.AdminDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AdminDecisionController extends BaseController implements AdminDecisionApi {

    private final AdminDecisionService adminDecisionService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<List<AdminDecisionRecordResponse>>> getDecisionReport(UUID userId, DecisionType type, Integer days) {
        return ok(adminDecisionService.getDecisionReport(userId, type, days));
    }
}
