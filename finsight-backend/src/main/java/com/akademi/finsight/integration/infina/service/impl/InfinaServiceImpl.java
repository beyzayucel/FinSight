package com.akademi.finsight.integration.infina.service.impl;

import com.akademi.finsight.integration.infina.client.InfinaServicesClient;
import com.akademi.finsight.integration.infina.client.dto.BenchmarkInfoData;
import com.akademi.finsight.integration.infina.client.dto.base.InfinaResponse;
import com.akademi.finsight.integration.infina.dto.response.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.mapper.InfinaMapper;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InfinaServiceImpl implements InfinaService {
	private final InfinaServicesClient infinaServicesClient;
	private final InfinaMapper infinaMapper;

	@Override
	public List<BenchmarkInfoResponse> getBenchmarkInfo(String fundCode,
														String beginPeriod,
														String endPeriod,
														String currency){
		InfinaResponse<BenchmarkInfoData> response = infinaServicesClient.getBenchmarkInfo(fundCode, beginPeriod, endPeriod, currency);
		return infinaMapper.toBenchmarkInfoResponseList(response.result().data().benchmarkInfos());
	}
}
