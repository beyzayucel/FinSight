package com.akademi.finsight.integration.infina.service.impl;

import com.akademi.finsight.integration.infina.client.InfinaServicesClient;
import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfoData;
import com.akademi.finsight.integration.infina.client.dto.base.InfinaResponse;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.exception.InfinaErrorType;
import com.akademi.finsight.integration.infina.exception.InfinaIntegrationException;
import com.akademi.finsight.integration.infina.mapper.InfinaMapper;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfinaServiceImpl implements InfinaService {

	private static final int INFINA_SUCCES_CODE = 200;
	private final InfinaServicesClient infinaServicesClient;
	private final InfinaMapper infinaMapper;

	@Override
	public List<BenchmarkInfoResponse> getBenchmarkInfo(String fundCode,
														String beginPeriod,
														String endPeriod,
														String currency){

		InfinaResponse<BenchmarkInfoData> response;
		try {
			response = infinaServicesClient.getBenchmarkInfo(fundCode, beginPeriod, endPeriod, currency);
		} catch (RestClientException e){
			log.error("Infina call failed: event=INFINA_UNAVAILABLE, fundCode={}", fundCode, e);
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_UNAVAILABLE, e);
		}

		if (response == null || response.result() == null || response.result().summary() == null || response.result().data() == null){
			log.warn("Infina returned an invalid response. event=INFINA_ERROR_RESPONSE, fundCode={}, response={}",fundCode, response);
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_ERROR_RESPONSE);
		}

		var	summary = response.result().summary();
		if (summary.resultCode() != INFINA_SUCCES_CODE){
			log.warn("Infina error response: event=INFINA_ERROR_RESPONSE, resultCode={}, resultMessage={}",
					summary.resultCode(), summary.resultMessage());
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_ERROR_RESPONSE);
		}

		return infinaMapper.toBenchmarkInfoResponseList(
				response.result().data().benchmarkInfos());
	}

	@Override
	public FundInfoResponse getFundInfo(String funCode,
										String date,
										String periods){
		InfinaResponse<FundInfoData> response;
		try {
			response = infinaServicesClient.getFundInfo(funCode, date, periods);
		} catch (RestClientException e){
			log.error("Infina call failed: event=INFINA_UNAVAILABLE, funCode{}", funCode,e);
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_UNAVAILABLE, e);
		}

		if (response == null || response.result() == null || response.result().summary() == null || response.result().data() == null){

			log.warn("Infina returned an invalid response, event=INFINA_RESPONSE, fundCode={}, response={}", funCode, response);
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_ERROR_RESPONSE);
		}

		var	summary = response.result().summary();
		if (summary.resultCode() != INFINA_SUCCES_CODE){

			log.warn("Infina error response: event=INFINA_ERROR_RESPONSE, resultCode={}, resultMessage={}", summary.resultCode(), summary.resultMessage());
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_ERROR_RESPONSE);
		}

		return infinaMapper.toFundInfoResponse(response.result().data());
	}
}
