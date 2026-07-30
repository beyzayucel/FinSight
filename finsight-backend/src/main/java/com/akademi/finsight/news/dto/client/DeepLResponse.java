package com.akademi.finsight.news.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeepLResponse(
        @JsonProperty("translations")
        List<DeepLTranslation> translations
) {
}
