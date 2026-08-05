package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.dto.request.FundDistributionRequest;
import com.akademi.finsight.fund.dto.response.FundDistributionResponse;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.FundDistribution;
import com.akademi.finsight.fund.exception.FundDistributionNotFoundException;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.mapper.FundDistributionMapper;
import com.akademi.finsight.fund.repository.FundDistributionRepository;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.service.FundDistributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FundDistributionServiceImpl implements FundDistributionService {

    private final FundDistributionRepository fundDistributionRepository;
    private final FundRepository fundRepository;
    private final FundDistributionMapper fundDistributionMapper;

    @Override
    public FundDistributionResponse create(FundDistributionRequest request) {
        Fund fund = getFund(request.fundId());

        FundDistribution entity = fundDistributionMapper.toEntity(request);
        entity.setFund(fund);

        FundDistribution saved = fundDistributionRepository.save(entity);
        log.info("Fund distribution created: id={}, fundId={}", saved.getId(), fund.getId());

        return fundDistributionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FundDistributionResponse getById(UUID id) {
        return fundDistributionMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundDistributionResponse> getAll() {
        return fundDistributionMapper.toResponseList(fundDistributionRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundDistributionResponse> getLatestByFundCode(String fundCode) {
        if (!fundRepository.existsByCode(fundCode)) {
            throw new FundNotFoundException();
        }
        return fundDistributionMapper.toResponseList(
                fundDistributionRepository.findLatestByFundCode(fundCode));
    }

    @Override
    public FundDistributionResponse update(UUID id, FundDistributionRequest request) {
        FundDistribution entity = getEntity(id);
        Fund fund = getFund(request.fundId());

        fundDistributionMapper.updateEntity(entity, request);
        entity.setFund(fund);

        FundDistribution saved = fundDistributionRepository.save(entity);
        log.info("Fund distribution updated: id={}", saved.getId());

        return fundDistributionMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!fundDistributionRepository.existsById(id)) {
            throw new FundDistributionNotFoundException();
        }
        fundDistributionRepository.deleteById(id);
        log.info("Fund distribution deleted: id={}", id);
    }

    private FundDistribution getEntity(UUID id) {
        return fundDistributionRepository.findById(id)
                .orElseThrow(FundDistributionNotFoundException::new);
    }

    private Fund getFund(UUID fundId) {
        return fundRepository.findById(fundId)
                .orElseThrow(FundNotFoundException::new);
    }
}
