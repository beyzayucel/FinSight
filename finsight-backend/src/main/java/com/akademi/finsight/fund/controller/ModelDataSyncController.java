package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.controller.api.ModelDataSyncApi;
import com.akademi.finsight.fund.dto.response.ModelDataSyncResponse;
import com.akademi.finsight.fund.service.ModelDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ModelDataSyncController extends BaseController implements ModelDataSyncApi {

    private final ModelDataSyncService modelDataSyncService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<ModelDataSyncResponse>> sync() {
        return ok(modelDataSyncService.sync());
    }
}
