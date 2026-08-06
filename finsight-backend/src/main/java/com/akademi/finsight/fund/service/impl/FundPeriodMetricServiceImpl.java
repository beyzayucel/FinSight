package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.dto.request.FundPeriodMetricRequest;
import com.akademi.finsight.fund.dto.response.FundPeriodMetricResponse;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.entity.FundPeriodMetric;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.exception.FundPeriodMetricAlreadyExistsException;
import com.akademi.finsight.fund.exception.FundPeriodMetricNotFoundException;
import com.akademi.finsight.fund.mapper.FundPeriodMetricMapper;
import com.akademi.finsight.fund.repository.FundPeriodMetricRepository;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.service.FundPeriodMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FundPeriodMetricServiceImpl implements FundPeriodMetricService {

    private final FundPeriodMetricRepository fundPeriodMetricRepository;
    private final FundRepository fundRepository;
    private final FundPeriodMetricMapper fundPeriodMetricMapper;

    @Override
    public FundPeriodMetricResponse create(FundPeriodMetricRequest request) {
        Fund fund = getFund(request.fundId());

        if (fundPeriodMetricRepository.existsByFundIdAndDataDateAndPeriod(
                fund.getId(), request.dataDate(), request.period())) {
            throw new FundPeriodMetricAlreadyExistsException();
        }

        FundPeriodMetric entity = fundPeriodMetricMapper.toEntity(request);
        entity.setFund(fund);
        entity.setFetchedAt(request.fetchedAt() != null ? request.fetchedAt() : Instant.now());

        FundPeriodMetric saved = fundPeriodMetricRepository.save(entity);
        log.info("Fund period metric created: id={}, fundId={}, dataDate={}, period={}",
                saved.getId(), fund.getId(), saved.getDataDate(), saved.getPeriod());

        return fundPeriodMetricMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FundPeriodMetricResponse getById(UUID id) {
        return fundPeriodMetricMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FundPeriodMetricResponse> getAll(Pageable pageable) {
        return fundPeriodMetricRepository.findAll(pageable).map(fundPeriodMetricMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundPeriodMetricResponse> getLatestByFundCode(String fundCode) {
        if (!fundRepository.existsByCode(fundCode)) {
            throw new FundNotFoundException();
        }
        return fundPeriodMetricMapper.toResponseList(
                fundPeriodMetricRepository.findLatestByFundCode(fundCode));
    }

    @Override
    public FundPeriodMetricResponse update(UUID id, FundPeriodMetricRequest request) {
        FundPeriodMetric entity = getEntity(id);
        Fund fund = getFund(request.fundId());

        if (fundPeriodMetricRepository.existsByFundIdAndDataDateAndPeriodAndIdNot(
                fund.getId(), request.dataDate(), request.period(), id)) {
            throw new FundPeriodMetricAlreadyExistsException();
        }

        fundPeriodMetricMapper.updateEntity(entity, request);
        entity.setFund(fund);
        if (request.fetchedAt() != null) {
            entity.setFetchedAt(request.fetchedAt());
        }

        FundPeriodMetric saved = fundPeriodMetricRepository.save(entity);
        log.info("Fund period metric updated: id={}", saved.getId());

        return fundPeriodMetricMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!fundPeriodMetricRepository.existsById(id)) {
            throw new FundPeriodMetricNotFoundException();
        }
        fundPeriodMetricRepository.deleteById(id);
        log.info("Fund period metric deleted: id={}", id);
    }

    private FundPeriodMetric getEntity(UUID id) {
        return fundPeriodMetricRepository.findById(id)
                .orElseThrow(FundPeriodMetricNotFoundException::new);
    }

    private Fund getFund(UUID fundId) {
        return fundRepository.findById(fundId)
                .orElseThrow(FundNotFoundException::new);
    }
}
