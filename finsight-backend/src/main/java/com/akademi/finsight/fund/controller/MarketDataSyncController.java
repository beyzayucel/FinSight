package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.controller.api.MarketDataSyncApi;
import com.akademi.finsight.fund.entity.MarketData;
import com.akademi.finsight.fund.service.MarketDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MarketDataSyncController extends BaseController implements MarketDataSyncApi {

    private final MarketDataSyncService marketSyncService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<MarketData>> sync() {
        return ok(marketSyncService.sync());
    }
}
