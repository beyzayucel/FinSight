package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.fund.controller.api.FundScenarioApi;
import com.akademi.finsight.fund.dto.request.ManualScenarioRequest;
import com.akademi.finsight.fund.dto.response.ManualScenarioResponse;
import com.akademi.finsight.fund.service.ManualScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class FundScenarioController extends BaseController implements FundScenarioApi {

    private final ManualScenarioService manualScenarioService;

    @Override
    public ResponseEntity<ApiStandardResponse<Void>> applyManualScenario(String email, ManualScenarioRequest request) {
        manualScenarioService.applyManualScenario(email, request);
        return created();
    }

    @Override
    public ResponseEntity<ApiStandardResponse<List<ManualScenarioResponse>>> getScenarioHistory(String email, UUID fundId) {
        return ok(manualScenarioService.getScenarioHistory(email, fundId));
    }
}
