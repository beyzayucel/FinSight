package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.request.FundRequest;
import com.akademi.finsight.fund.dto.response.FundResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FundService {

    FundResponse create(FundRequest request);

    FundResponse getById(UUID id);

    Page<FundResponse> getAll(Pageable pageable);

    FundResponse update(UUID id, FundRequest request);

    void delete(UUID id);
}
