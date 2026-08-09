package com.akademi.finsight.fund.dto.response;

import com.akademi.finsight.fund.entity.FundPriceData;
import com.akademi.finsight.fund.entity.MarketData;

public record ModelDataSyncResponse(
        MarketData marketData,
        FundPriceData fundPriceData
) {}
