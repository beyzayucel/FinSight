package com.akademi.finsight.integration.infina.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.integration.infina.client.dto.benchmark.BenchmarkInfo;
import com.akademi.finsight.integration.infina.client.dto.fund.CumulativeReturnEntry;
import com.akademi.finsight.integration.infina.client.dto.fund.FundDailyReturn;
import com.akademi.finsight.integration.infina.client.dto.fund.FundInfoData;
import com.akademi.finsight.integration.infina.client.dto.fund.FundPortfolioAllocation;
import com.akademi.finsight.integration.infina.client.dto.fx.FxPriceRow;
import com.akademi.finsight.integration.infina.client.dto.index.IndexPriceRow;
import com.akademi.finsight.integration.infina.client.dto.economic.EconomicPriceRow;
import com.akademi.finsight.integration.infina.dto.response.benchmark.BenchmarkInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.CumulativeReturnEntryResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundDailyReturnResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundInfoResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundPortfolioAllocationResponse;
import com.akademi.finsight.integration.infina.dto.response.fund.FundReturnResponse;
import com.akademi.finsight.integration.infina.dto.response.fx.FxPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.index.IndexPriceResponse;
import com.akademi.finsight.integration.infina.dto.response.economic.EconomicPriceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Mapper(config = BaseMapperConfig.class,
		nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface InfinaMapper {

	List<FxPriceResponse> toFxPriceResponseList(List<FxPriceRow> rows);

	List<IndexPriceResponse> toIndexPriceResponseList(List<IndexPriceRow> rows);

	List<EconomicPriceResponse> toEconomicPriceResponseList(List<EconomicPriceRow> rows);

	@Mapping(target = "benchmarkYield", source = "bmYield")
	BenchmarkInfoResponse toBenchmarkInfoResponse(BenchmarkInfo benchmarkInfo);

	List<BenchmarkInfoResponse> toBenchmarkInfoResponseList(List<BenchmarkInfo> benchmarkInfos);

	@Mapping(target = "periodReturns", expression = "java(toFundReturns(fundInfoData))")
	@Mapping(target = "name", source = "fundDetail.description")
	@Mapping(target = "totalMarketPrice", source = "fundDetail.totalMv")
	@Mapping(target = "assetDistribution", source = "fundDistribution")
	@Mapping(target = "investorCount", source = "fundDetail.investorCount")
	FundInfoResponse toFundInfoResponse(FundInfoData fundInfoData);

	@Mapping(target = "ratio", source = "groupPercentage")
	@Mapping(target = "assetType", source = "mkType")
	FundPortfolioAllocationResponse toFundPortfolioAllocationResponse(FundPortfolioAllocation fundPortfolioAllocation);

	List<FundPortfolioAllocationResponse> toFundPortfolioAllocationResponseList(List<FundPortfolioAllocation> allocations);

	FundDailyReturnResponse toFundDailyReturnResponse(FundDailyReturn fundDailyReturn);

	CumulativeReturnEntryResponse toCumulativeReturnEntryResponse(CumulativeReturnEntry entry);

	List<CumulativeReturnEntryResponse> toCumulativeReturnEntryResponseList(List<CumulativeReturnEntry> entries);

	default List<FundReturnResponse> toFundReturns(FundInfoData fundInfoData) {
		List<String> periods = fundInfoData.periods();
		List<BigDecimal> fundReturn = fundInfoData.fundReturn();
		if (periods == null || fundReturn == null) {
			return List.of();
		}
		List<LocalDate> beginDates = fundInfoData.fundBeginDates();
		List<BigDecimal> fundBenchmark = fundInfoData.fundBenchmark();
		int size = Math.min(periods.size(), fundReturn.size());
		List<FundReturnResponse> returns = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			LocalDate beginDate = (beginDates != null && i < beginDates.size()) ? beginDates.get(i) : null;
			BigDecimal benchmarkReturn = (fundBenchmark != null && i < fundBenchmark.size())
					? fundBenchmark.get(i) : null;
			returns.add(new FundReturnResponse(periods.get(i), fundReturn.get(i), beginDate, benchmarkReturn));
		}
		return returns;
	}
}
