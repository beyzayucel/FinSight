package com.akademi.finsight.ai.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/// istek atıyor
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StockBreakdownRatios(
         @JsonProperty("ASELS") BigDecimal asels,
         @JsonProperty("BIMAS") BigDecimal bimas,
         @JsonProperty("THYAO") BigDecimal thyao,
         @JsonProperty("AKBNK") BigDecimal akbnk,
         @JsonProperty("TUPRS") BigDecimal tuprs,
         @JsonProperty("YKBNK") BigDecimal ykbnk,
         @JsonProperty("ISCTR") BigDecimal isctr,
         @JsonProperty("KCHOL") BigDecimal kchol,
         @JsonProperty("SAHOL") BigDecimal sahol,
         @JsonProperty("TCELL") BigDecimal tcell,
         @JsonProperty("OTHER") BigDecimal other
) {

}
