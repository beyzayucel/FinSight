package com.akademi.finsight.news.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsApiResponse(
        @JsonProperty("offset")
        Integer offset,
        @JsonProperty("number")
        Integer number,
        @JsonProperty("available")
        Integer available,
        @JsonProperty("news")
        List<NewsItem> news
) {
}
