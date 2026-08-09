package com.akademi.finsight.fund.dto.response;

public record ModelDataSyncResponse(
        MarketDataResponse marketData,
        FundPriceDataResponse fundPriceData
) {}
