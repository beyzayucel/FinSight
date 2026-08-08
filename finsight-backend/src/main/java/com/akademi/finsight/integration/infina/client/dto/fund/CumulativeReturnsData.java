package com.akademi.finsight.integration.infina.client.dto.fund;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CumulativeReturnsData(
        @JsonProperty("CumulativeReturns") List<CumulativeReturnEntry> cumulativeReturns
) {}
