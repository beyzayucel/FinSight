package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.fund.controller.api.FundStockAllocationApi;
import com.akademi.finsight.fund.dto.request.FundStockAllocationRequest;
import com.akademi.finsight.fund.dto.response.FundStockAllocationResponse;
import com.akademi.finsight.fund.service.FundStockAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FundStockAllocationController extends BaseController implements FundStockAllocationApi {

    private final FundStockAllocationService fundStockAllocationService;

    @Override
    public ResponseEntity<ApiStandardResponse<FundStockAllocationResponse>> create(
            FundStockAllocationRequest request) {
        return created(fundStockAllocationService.create(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundStockAllocationResponse>> getById(UUID id) {
        return ok(fundStockAllocationService.getById(id));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<PageResponse<FundStockAllocationResponse>>> getAll(Pageable pageable) {
        return ok(PageResponse.of(fundStockAllocationService.getAll(pageable)));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<List<FundStockAllocationResponse>>> getByFundCodeAndPeriod(
            String fundCode, String period) {
        return ok(fundStockAllocationService.getByFundCodeAndPeriod(fundCode, period));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundStockAllocationResponse>> update(
            UUID id, FundStockAllocationRequest request) {
        return ok(fundStockAllocationService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        fundStockAllocationService.delete(id);
        return noContent();
    }
}
