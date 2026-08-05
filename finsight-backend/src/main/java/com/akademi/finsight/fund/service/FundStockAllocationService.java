package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.request.FundStockAllocationRequest;
import com.akademi.finsight.fund.dto.response.FundStockAllocationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FundStockAllocationService {

    FundStockAllocationResponse create(FundStockAllocationRequest request);

    FundStockAllocationResponse getById(UUID id);

    Page<FundStockAllocationResponse> getAll(Pageable pageable);

    List<FundStockAllocationResponse> getByFundCodeAndPeriod(String fundCode, String period);

    FundStockAllocationResponse update(UUID id, FundStockAllocationRequest request);

    void delete(UUID id);
}
