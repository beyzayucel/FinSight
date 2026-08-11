package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.fund.controller.api.FundPeriodMetricApi;
import com.akademi.finsight.fund.dto.request.FundPeriodMetricRequest;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FundPeriodMetricController extends BaseController implements FundPeriodMetricApi {

    private final FundPeriodMetricService fundPeriodMetricService;

    @Override
    public ResponseEntity<ApiStandardResponse<FundPeriodMetricResponse>> create(
            FundPeriodMetricRequest request) {
        return created(fundPeriodMetricService.create(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundPeriodMetricResponse>> getById(UUID id) {
        return ok(fundPeriodMetricService.getById(id));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<PageResponse<FundPeriodMetricResponse>>> getAll(Pageable pageable) {
        return ok(PageResponse.of(fundPeriodMetricService.getAll(pageable)));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<List<FundPeriodMetricResponse>>> getLatestByFundCode(String fundCode) {
        return ok(fundPeriodMetricService.getLatestByFundCode(fundCode));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundPeriodMetricResponse>> update(
            UUID id, FundPeriodMetricRequest request) {
        return ok(fundPeriodMetricService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        fundPeriodMetricService.delete(id);
        return noContent();
    }
}
