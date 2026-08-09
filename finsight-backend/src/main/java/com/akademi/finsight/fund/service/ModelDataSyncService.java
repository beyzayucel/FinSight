package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.response.ModelDataSyncResponse;
import com.akademi.finsight.fund.entity.FundPriceData;
import com.akademi.finsight.fund.entity.MarketData;

public interface ModelDataSyncService {

    ModelDataSyncResponse sync();

    MarketData syncMarketData();

    FundPriceData syncFundPriceData();
}
