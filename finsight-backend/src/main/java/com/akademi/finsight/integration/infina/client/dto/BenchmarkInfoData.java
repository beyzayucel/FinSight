package com.akademi.finsight.integration.infina.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BenchmarkInfoData(
		@JsonProperty("BenchmarkInfo") List<BenchmarkInfo> benchmarkInfos
){}
