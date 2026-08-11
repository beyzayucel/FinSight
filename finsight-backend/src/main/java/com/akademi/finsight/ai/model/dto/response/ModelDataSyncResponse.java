package com.akademi.finsight.ai.model.dto.response;

public record ModelDataSyncResponse(
        MarketDataResponse marketData,
        FundPriceDataResponse fundPriceData
) {}
