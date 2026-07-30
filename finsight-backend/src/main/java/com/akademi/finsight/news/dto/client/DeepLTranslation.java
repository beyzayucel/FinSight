package com.akademi.finsight.news.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeepLTranslation(
        @JsonProperty("text")
        String text
) {
}
