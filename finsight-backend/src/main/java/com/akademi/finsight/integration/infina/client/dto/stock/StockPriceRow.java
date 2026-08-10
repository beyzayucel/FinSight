package com.akademi.finsight.integration.infina.client.dto.stock;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Infina HisseFiyat satiri.
 * <p>
 * Dikkat: {@code recordDate} bu kaydin Infina'ya senkronize edildigi zamandir, islem tarihi degildir
 * (ornegin data_date=2013-06-17 olan bir kayitta record_date=2021-05-06 gorulebilir). Getiri hesabinda
 * daima {@code dataDate} kullanilmali, {@code recordDate} yok sayilmalidir.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record StockPriceRow(
        String recordId,
        String recordDate,
        String assetCode,
        BigDecimal closePrice,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        LocalDate dataDate
) {}
