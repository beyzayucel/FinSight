package com.akademi.finsight.fund.dto.request;

import lombok.Builder;

@Builder
public record FundModelInputRequest(
        float stockWeight,
        float repoWeight,
        float futureWeight,
        float fundWeight,

        float usdReturn,
        float goldReturn,
        float brentReturn,
        float us10yReturn,
        float cdsSpreadBps,
        float annualInflation,
        float policyRate
)

{}
