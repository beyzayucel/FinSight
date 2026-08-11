package com.akademi.finsight.integration.infina.client.dto.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BenchmarkInfoData(
		@JsonProperty("BenchmarkInfo") List<BenchmarkInfo> benchmarkInfos
){}
