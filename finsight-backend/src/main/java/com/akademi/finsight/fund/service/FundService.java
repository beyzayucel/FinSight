package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.request.FundRequest;
import com.akademi.finsight.fund.dto.response.FundResponse;

import java.util.List;
import java.util.UUID;

public interface FundService {

    FundResponse create(FundRequest request);

    FundResponse getById(UUID id);

    List<FundResponse> getAll();

    FundResponse update(UUID id, FundRequest request);

    void delete(UUID id);
}
