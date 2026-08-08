package com.akademi.finsight.integration.infina.client.dto.fx;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public record FxPriceData(
        @JsonProperty("DovizFiyat") List<FxPriceRow> fxPrices
) {}
