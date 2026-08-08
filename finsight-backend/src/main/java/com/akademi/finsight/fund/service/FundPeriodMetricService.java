package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.request.FundPeriodMetricRequest;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FundPeriodMetricService {

    FundPeriodMetricResponse create(FundPeriodMetricRequest request);

    FundPeriodMetricResponse getById(UUID id);

    Page<FundPeriodMetricResponse> getAll(Pageable pageable);

    List<FundPeriodMetricResponse> getLatestByFundCode(String fundCode);

    FundPeriodMetricResponse getLatestByFundCodeAndPeriod(String fundCode, String period);

    FundPeriodMetricResponse update(UUID id, FundPeriodMetricRequest request);

    void delete(UUID id);
}
