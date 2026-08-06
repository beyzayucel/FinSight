package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.dto.request.FundRequest;
import com.akademi.finsight.fund.dto.response.FundResponse;
import com.akademi.finsight.fund.entity.Fund;
import com.akademi.finsight.fund.exception.FundCodeAlreadyExistsException;
import com.akademi.finsight.fund.exception.FundNotFoundException;
import com.akademi.finsight.fund.mapper.FundMapper;
import com.akademi.finsight.fund.repository.FundRepository;
import com.akademi.finsight.fund.service.FundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FundServiceImpl implements FundService {

    private final FundRepository fundRepository;
    private final FundMapper fundMapper;

    @Override
    public FundResponse create(FundRequest request) {
        if (fundRepository.existsByCode(request.code())) {
            throw new FundCodeAlreadyExistsException();
        }

        Fund saved = fundRepository.save(fundMapper.toEntity(request));
        log.info("Fund created: id={}, code={}", saved.getId(), saved.getCode());

        return fundMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FundResponse getById(UUID id) {
        return fundMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FundResponse getByCode(String code) {
        return fundMapper.toResponse(fundRepository.findByCode(code)
                .orElseThrow(FundNotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FundResponse> getAll(Pageable pageable) {
        return fundRepository.findAll(pageable).map(fundMapper::toResponse);
    }

    @Override
    public FundResponse update(UUID id, FundRequest request) {
        Fund entity = getEntity(id);

        if (fundRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new FundCodeAlreadyExistsException();
        }

        fundMapper.updateEntity(entity, request);
        Fund saved = fundRepository.save(entity);
        log.info("Fund updated: id={}", saved.getId());

        return fundMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!fundRepository.existsById(id)) {
            throw new FundNotFoundException();
        }
        fundRepository.deleteById(id);
        log.info("Fund deleted: id={}", id);
    }

    private Fund getEntity(UUID id) {
        return fundRepository.findById(id)
                .orElseThrow(FundNotFoundException::new);
    }
}
