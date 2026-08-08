package com.akademi.finsight.integration.infina.client.dto.fund;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FundDailyReturnData(
        @JsonProperty("FonGunlukGetiri") List<FundDailyReturn> fundDailyReturns
) {}
