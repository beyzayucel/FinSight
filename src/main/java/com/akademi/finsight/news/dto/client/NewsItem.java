package com.akademi.finsight.news.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsItem(
        @JsonProperty("title")
        String title,
        @JsonProperty("url")
        String url,
        @JsonProperty("publish_date")
        String publishDate
) implements Serializable {
}
