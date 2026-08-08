package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.controller.api.MacroDataSyncApi;
import com.akademi.finsight.fund.entity.MacroData;
import com.akademi.finsight.fund.service.MacroDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class MacroDataSyncController extends BaseController implements MacroDataSyncApi {

    private final MacroDataSyncService macroDataSyncService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<MacroData>> sync() {
        return ok(macroDataSyncService.sync(LocalDate.now()));
    }
}
