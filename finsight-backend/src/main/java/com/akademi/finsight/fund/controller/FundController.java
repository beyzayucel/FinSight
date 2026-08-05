package com.akademi.finsight.fund.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.fund.controller.api.FundApi;
import com.akademi.finsight.fund.dto.request.FundRequest;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.service.FundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FundController extends BaseController implements FundApi {

    private final FundService fundService;

    @Override
    public ResponseEntity<ApiStandardResponse<FundResponse>> create(FundRequest request) {
        return created(fundService.create(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundResponse>> getById(UUID id) {
        return ok(fundService.getById(id));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<PageResponse<FundResponse>>> getAll(Pageable pageable) {
        return ok(PageResponse.of(fundService.getAll(pageable)));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<FundResponse>> update(UUID id, FundRequest request) {
        return ok(fundService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        fundService.delete(id);
        return noContent();
    }
}
