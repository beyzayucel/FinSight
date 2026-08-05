package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.request.FundDistributionRequest;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;

import java.util.List;
import java.util.UUID;

public interface FundDistributionService {

    FundDistributionResponse create(FundDistributionRequest request);

    FundDistributionResponse getById(UUID id);

    List<FundDistributionResponse> getAll();

    List<FundDistributionResponse> getLatestByFundCode(String fundCode);

    FundDistributionResponse update(UUID id, FundDistributionRequest request);

    void delete(UUID id);
}
