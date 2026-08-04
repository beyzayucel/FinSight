package com.akademi.finsight.integration.infina.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfo;
import com.akademi.finsight.integration.infina.client.dto.fund.FundAssetDistribution;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfo;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundAssetDistributionResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface InfinaMapper {

	@Mapping(target = "benchmarkYield", source = "bmYield")
	BenchmarkInfoResponse toBenchmarkInfoResponse(BenchmarkInfo benchmarkInfo);
	List<BenchmarkInfoResponse> toBenchmarkInfoResponseList(List<BenchmarkInfo> benchmarkInfos);

	@Mapping(target = "totalMarketPrice", source = "fundDetail.totalMv")
	@Mapping(target = "assetDistribution", source = "fundDistribution")
	@Mapping(target = "fundDailyYield",
			expression = "java(fundInfoData.fundReturn() == null || fundInfoData.fundReturn().isEmpty() ? null : fundInfoData.fundReturn().get(0))")
	FundInfoResponse toFundInfoResponse(FundInfoData fundInfoData);
}
