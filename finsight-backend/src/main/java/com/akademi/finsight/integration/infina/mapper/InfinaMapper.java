package com.akademi.finsight.integration.infina.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.integration.infina.client.dto.BenchmarkInfo;
import com.akademi.finsight.integration.infina.dto.response.BenchmarkInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface InfinaMapper {

	@Mapping(target = "benchmarkYield", source = "bmYield")
	BenchmarkInfoResponse toBenchmarkInfoResponse(BenchmarkInfo benchmarkInfo);

	List<BenchmarkInfoResponse> toBenchmarkInfoResponseList(List<BenchmarkInfo> benchmarkInfos);
}
