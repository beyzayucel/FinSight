package com.akademi.finsight.integration.infina.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfo;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.client.dto.fund.FundPortfolioAllocation;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundReturnResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Mapper(config = BaseMapperConfig.class,
		nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface InfinaMapper {

	@Mapping(target = "benchmarkYield", source = "bmYield")
	BenchmarkInfoResponse toBenchmarkInfoResponse(BenchmarkInfo benchmarkInfo);

	List<BenchmarkInfoResponse> toBenchmarkInfoResponseList(List<BenchmarkInfo> benchmarkInfos);

	@Mapping(target = "periodReturns", expression = "java(toFundReturns(fundInfoData))")
	@Mapping(target = "totalMarketPrice", source = "fundDetail.totalMv")
	@Mapping(target = "assetDistribution", source = "fundDistribution")
	FundInfoResponse toFundInfoResponse(FundInfoData fundInfoData);

	@Mapping(target = "ratio", source = "groupPercentage")
	FundPortfolioAllocationResponse toFundPortfolioAllocationResponse(FundPortfolioAllocation fundPortfolioAllocation);

	List<FundPortfolioAllocationResponse> toFundPortfolioAllocationResponseList(List<FundPortfolioAllocation> allocations);

	default List<FundReturnResponse> toFundReturns(FundInfoData fundInfoData) {
		List<String> periods = fundInfoData.periods();
		List<BigDecimal> fundReturn = fundInfoData.fundReturn();
		if (periods == null || fundReturn == null) {
			return List.of();
		}
		int size = Math.min(periods.size(), fundReturn.size());
		List<FundReturnResponse> returns = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			returns.add(new FundReturnResponse(periods.get(i), fundReturn.get(i)));
		}
		return returns;
	}
}
