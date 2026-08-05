package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.dto.request.FundStockAllocationRequest;
import com.akademi.finsight.fund.dto.response.FundStockAllocationResponse;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.FundStockAllocation;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.exception.FundStockAllocationAlreadyExistsException;
import com.akademi.finsight.fund.exception.FundStockAllocationNotFoundException;
import com.akademi.finsight.fund.mapper.FundStockAllocationMapper;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.repository.FundStockAllocationRepository;
import com.akademi.finsight.fund.service.FundStockAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FundStockAllocationServiceImpl implements FundStockAllocationService {

    private final FundStockAllocationRepository fundStockAllocationRepository;
    private final FundRepository fundRepository;
    private final FundStockAllocationMapper fundStockAllocationMapper;

    @Override
    public FundStockAllocationResponse create(FundStockAllocationRequest request) {
        Fund fund = getFund(request.fundId());

        if (fundStockAllocationRepository.existsByFundIdAndPeriodAndAssetCode(
                fund.getId(), request.period(), request.assetCode())) {
            throw new FundStockAllocationAlreadyExistsException();
        }

        FundStockAllocation entity = fundStockAllocationMapper.toEntity(request);
        entity.setFund(fund);

        FundStockAllocation saved = fundStockAllocationRepository.save(entity);
        log.info("Fund stock allocation created: id={}, fundId={}, period={}, assetCode={}",
                saved.getId(), fund.getId(), saved.getPeriod(), saved.getAssetCode());

        return fundStockAllocationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FundStockAllocationResponse getById(UUID id) {
        return fundStockAllocationMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FundStockAllocationResponse> getAll(Pageable pageable) {
        return fundStockAllocationRepository.findAll(pageable).map(fundStockAllocationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundStockAllocationResponse> getByFundCodeAndPeriod(String fundCode, String period) {
        if (!fundRepository.existsByCode(fundCode)) {
            throw new FundNotFoundException();
        }
        return fundStockAllocationMapper.toResponseList(
                fundStockAllocationRepository.findByFundCodeAndPeriod(fundCode, period));
    }

    @Override
    public FundStockAllocationResponse update(UUID id, FundStockAllocationRequest request) {
        FundStockAllocation entity = getEntity(id);
        Fund fund = getFund(request.fundId());

        if (fundStockAllocationRepository.existsByFundIdAndPeriodAndAssetCodeAndIdNot(
                fund.getId(), request.period(), request.assetCode(), id)) {
            throw new FundStockAllocationAlreadyExistsException();
        }

        fundStockAllocationMapper.updateEntity(entity, request);
        entity.setFund(fund);

        FundStockAllocation saved = fundStockAllocationRepository.save(entity);
        log.info("Fund stock allocation updated: id={}", saved.getId());

        return fundStockAllocationMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!fundStockAllocationRepository.existsById(id)) {
            throw new FundStockAllocationNotFoundException();
        }
        fundStockAllocationRepository.deleteById(id);
        log.info("Fund stock allocation deleted: id={}", id);
    }

    private FundStockAllocation getEntity(UUID id) {
        return fundStockAllocationRepository.findById(id)
                .orElseThrow(FundStockAllocationNotFoundException::new);
    }

    private Fund getFund(UUID fundId) {
        return fundRepository.findById(fundId)
                .orElseThrow(FundNotFoundException::new);
    }
}
