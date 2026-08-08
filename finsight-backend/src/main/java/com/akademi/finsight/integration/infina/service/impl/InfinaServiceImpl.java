package com.akademi.finsight.integration.infina.service.impl;

import com.akademi.finsight.integration.infina.client.InfinaServicesClient;
import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfoData;
import com.akademi.finsight.integration.infina.client.dto.base.InfinaResponse;
import com.akademi.finsight.integration.infina.client.dto.fund.FundDailyReturnData;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.client.dto.fund.FundPortfolioAllocationData;
import com.akademi.finsight.integration.infina.client.dto.fx.FxPriceData;
import com.akademi.finsight.integration.infina.client.dto.index.IndexPriceData;
import com.akademi.finsight.integration.infina.client.dto.economic.EconomicPriceData;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundDailyReturnResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
import com.akademi.finsight.integration.infina.dto.response.fx.FxPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.index.IndexPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.economic.EconomicPriceResponse;
import com.akademi.finsight.integration.infina.exception.InfinaErrorType;
import com.akademi.finsight.integration.infina.exception.InfinaIntegrationException;
import com.akademi.finsight.integration.infina.mapper.InfinaMapper;
import com.akademi.finsight.integration.infina.service.InfinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfinaServiceImpl implements InfinaService {

	private static final int INFINA_SUCCESS_CODE = 200;
	private final InfinaServicesClient infinaServicesClient;
	private final InfinaMapper infinaMapper;

	@Override
	public List<FxPriceResponse> getFxPrices(String assetCode, String dataDate) {
		FxPriceData data = callInfina(
				() -> infinaServicesClient.getFxPrices(assetCode, dataDate),
				assetCode != null ? assetCode : "ALL");
		return infinaMapper.toFxPriceResponseList(data.fxPrices());
	}

	@Override
	public List<IndexPriceResponse> getIndexPrices(String assetCode, String dataDate) {
		IndexPriceData data = callInfina(
				() -> infinaServicesClient.getIndexPrices(assetCode, dataDate),
				assetCode != null ? assetCode : "ALL");
		return infinaMapper.toIndexPriceResponseList(data.indexPrices());
	}

	@Override
	public List<EconomicPriceResponse> getEconomicPrices(String assetCode, String dataDate) {
		EconomicPriceData data = callInfina(
				() -> infinaServicesClient.getEconomicPrices(assetCode, dataDate),
				assetCode != null ? assetCode : "ALL");
		return infinaMapper.toEconomicPriceResponseList(data.economicPrices());
	}

	@Override
	public List<BenchmarkInfoResponse> getBenchmarkInfo(String fundCode,
														String beginPeriod,
														String endPeriod,
														String currency){

		BenchmarkInfoData data = callInfina(
				() -> infinaServicesClient.getBenchmarkInfo(fundCode, beginPeriod, endPeriod, currency),
				fundCode);

		return infinaMapper.toBenchmarkInfoResponseList(data.benchmarkInfos());
	}

	@Override
	public FundInfoResponse getFundInfo(String fundCode,
										String date,
										String periods){
		FundInfoData data = callInfina(
				() -> infinaServicesClient.getFundInfo(fundCode, date, periods),
				fundCode);

		return infinaMapper.toFundInfoResponse(data);
	}

	@Override
	public List<FundPortfolioAllocationResponse> getFundPortfolioAllocation(String fundCode,
																			String period,
																			Long disclosureId){
		FundPortfolioAllocationData data = callInfina(
				() -> infinaServicesClient.getFundPortfolioAllocation(fundCode, period, disclosureId),
				fundCode);

		return infinaMapper.toFundPortfolioAllocationResponseList(data.allocations());
	}

	//TODO: Consider refactoring this method
	private <T> T callInfina(Supplier<InfinaResponse<T>> call, String fundCode) {
		InfinaResponse<T> response;
		try {
			response = call.get();
		} catch (RestClientResponseException e) {
			log.warn("Infina returned an error status: event=INFINA_ERROR_RESPONSE, fundCode={}, status={}",
					fundCode, e.getStatusCode(), e);
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_ERROR_RESPONSE, e);
		} catch (RestClientException e) {
			log.error("Infina call failed: event=INFINA_UNAVAILABLE, fundCode={}", fundCode, e);
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_UNAVAILABLE, e);
		}

		if (response == null || response.result() == null
				|| response.result().summary() == null || response.result().data() == null) {
			log.warn("Infina returned an invalid response: event=INFINA_ERROR_RESPONSE, fundCode={}, response={}",
					fundCode, response);
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_ERROR_RESPONSE);
		}

		InfinaResponse.Summary summary = response.result().summary();
		if (summary.resultCode() != INFINA_SUCCESS_CODE) {
			log.warn("Infina error response: event=INFINA_ERROR_RESPONSE, fundCode={}, resultCode={}, resultMessage={}",
					fundCode, summary.resultCode(), summary.resultMessage());
			throw new InfinaIntegrationException(InfinaErrorType.INFINA_ERROR_RESPONSE);
		}

		return response.result().data();
	}


	@Override
	public FundDailyReturnResponse getFundDailyReturn(String fundCode, String dates) {
		FundDailyReturnData data = callInfina(
				() -> infinaServicesClient.getFundDailyReturn(fundCode, dates),
				fundCode);

		return data.fundDailyReturns().stream()
				.findFirst()
				.map(infinaMapper::toFundDailyReturnResponse)
				.orElseThrow(() -> new InfinaIntegrationException(InfinaErrorType.INFINA_ERROR_RESPONSE));
	}

}
