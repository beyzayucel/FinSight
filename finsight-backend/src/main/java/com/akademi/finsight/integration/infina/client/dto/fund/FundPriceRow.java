package com.akademi.finsight.integration.infina.client.dto.fund;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FundPriceRow(
        @JsonAlias({"record_id", "recordId"}) String recordId,
        @JsonAlias({"fund_code", "fundCode"}) String fundCode,
        @JsonAlias({"isin_code", "isinCode"}) String isinCode,
        @JsonAlias({"data_date", "dataDate", "tarih", "fund_date", "fundDate", "date"}) LocalDate dataDate,
        @JsonAlias({"price", "fiyat"}) BigDecimal price,
        @JsonAlias({"aktif_deger", "active_value", "activeValue", "total_market_price"}) BigDecimal activeValue,
        @JsonAlias({"portfoy_degeri", "portfolio_value", "portfolioValue"}) BigDecimal portfolioValue,
        @JsonAlias({"hazir_deger", "cash_value", "cashValue"}) BigDecimal cashValue,
        @JsonAlias({"kisi_sayisi", "investor_count", "investorCount"}) BigDecimal investorCount
) {}
