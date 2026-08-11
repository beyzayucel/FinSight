package com.akademi.finsight.ai.dto;

import com.akademi.finsight.ai.dto.response.StockBreakdownResponse;
import com.akademi.finsight.ai.dto.response.TradeLegResponse;
import com.akademi.finsight.ai.dto.response.WeightsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gerçek bir emir değildir; seçilen action'ın mevcut dağılıma stateless
 * uygulanmasının simülasyonudur. clipped=true ise action mevcut dağılımda
 * uygulanamamış, weights_before == weights_after ve trade_legs boştur.
 */
public record ProposedTransition(
        @JsonProperty("weights_before") WeightsResponse weightsBefore,
        @JsonProperty("weights_after") WeightsResponse weightsAfter,
        @JsonProperty("weights_sum_ratio") BigDecimal weightsSumRatio,
        @JsonProperty("clipped") Boolean clipped,
        @JsonProperty("trade_amount_ratio") BigDecimal tradeAmountRatio,
        @JsonProperty("trade_legs") List<TradeLegResponse> tradeLegs,
        @JsonProperty("stock_breakdown_before_ratios") StockBreakdownResponse stockBreakdownBeforeRatios,
        @JsonProperty("stock_breakdown_after_ratios") StockBreakdownResponse stockBreakdownAfterRatios
) {
}
