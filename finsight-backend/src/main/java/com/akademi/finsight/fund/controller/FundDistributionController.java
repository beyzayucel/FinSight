package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.fund.controller.api.FundDistributionApi;
import com.akademi.finsight.fund.dto.request.FundDistributionRequest;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.service.FundDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FundDistributionController extends BaseController implements FundDistributionApi {

    private final FundDistributionService fundDistributionService;

    @Override
    public ResponseEntity<ApiStandardResponse<FundDistributionResponse>> create(
            FundDistributionRequest request) {
        return created(fundDistributionService.create(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundDistributionResponse>> getById(UUID id) {
        return ok(fundDistributionService.getById(id));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<PageResponse<FundDistributionResponse>>> getAll(Pageable pageable) {
        return ok(PageResponse.of(fundDistributionService.getAll(pageable)));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<List<FundDistributionResponse>>> getLatestByFundCode(String fundCode) {
        return ok(fundDistributionService.getLatestByFundCode(fundCode));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundDistributionResponse>> update(
            UUID id, FundDistributionRequest request) {
        return ok(fundDistributionService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        fundDistributionService.delete(id);
        return noContent();
    }
}
