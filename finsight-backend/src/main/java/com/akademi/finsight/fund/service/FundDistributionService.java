package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.request.FundDistributionRequest;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FundDistributionService {

    FundDistributionResponse create(FundDistributionRequest request);

    FundDistributionResponse getById(UUID id);

    Page<FundDistributionResponse> getAll(Pageable pageable);

    List<FundDistributionResponse> getLatestByFundCode(String fundCode);

    FundDistributionResponse update(UUID id, FundDistributionRequest request);

    void delete(UUID id);
}
