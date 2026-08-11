package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.controller.api.FundSyncApi;
import com.akademi.finsight.fund.dto.response.FundSyncResponse;
import com.akademi.finsight.fund.service.FundSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FundSyncController extends BaseController implements FundSyncApi {

    private final FundSyncService fundSyncService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<FundSyncResponse>> sync() {
        return ok(fundSyncService.sync());
    }
}
